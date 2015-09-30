# Client Code Samples #

## Logging in and creating a purl ##

### Perl ###
This sample was provided by David Wood.  Please modify the appropriate variables before running.
It creates a single PURL on a server.  For batch operations, see the next script, below.

```
#!/usr/bin/perl -w
#use strict;

############################################################
#  sendbatchpurl.pl
#
#  David Wood (david@zepheira.com)
#  January 2010
#
#  Script to create a single PURL on a PURLz v1.x server.
#
#  Copyright 2010 Zepheira LLC.  Licensed under the Apache License,
#  Version 2.0 (the "License"); you may not use this file except in compliance
#  with the License. You may obtain a copy of the License at
#  http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
#  or agreed to in writing, software distributed under the License is distributed
#  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
#  express or implied. See the License for the specific language governing
#  permissions and limitations under the License.
#
###########################################################
use LWP::UserAgent;
use HTTP::Cookies;
use HTTP::Request::Common qw(GET PUT POST);

###############################
# CHANGE THESE VARS AS NEEDED #
###############################
my $server = 'localhost:8080';
my $userid = 'userid';
my $passwd = 'password';
my $authurl = "http://$server/admin/login/login-submit.bsh";
my $workurl = "http://$server/admin/purls";
my $cookiefile = 'lwpcookies.txt';
my $useoldfile = 0;
my $xml =
'<?xml version="1.0" encoding="ISO-8859-1"?>
<purls>
  <purl id="/ontology/ADW" type="partial">
    <maintainers>
      <uid>natasha</uid>
    </maintainers>
    <target url="http://bioportal.bioontology.org/virtual/1002"/>
  </purl>
</purls>
';

# set up user agent with a cookie jar
my $res;
my $req;
my $ua = LWP::UserAgent->new();
my $cookie_jar = HTTP::Cookies->new(
  file     => 'lwpcookies.txt',
  autosave => 1,
  ignore_discard => 1,
);
$ua->cookie_jar($cookie_jar);

print STDERR "no old cookie jar file, so asking for new authorization\n"
   if ($useoldfile && ! -e $cookiefile);

print STDERR "set up cookie jar\n";
unless ($useoldfile && -e $cookiefile) {
  # try the authorization call, saving the cookie
  $req = POST($authurl, [id       => $userid,
                        passwd   => $passwd,
                        referrer => '/docs/index.html',
  ]);
  print STDERR "make auth request\n";
  $res = $ua->request($req);
  print STDERR "save cookie jar file\n";
  $cookie_jar->save();
  print STDERR "auth cookies retrieved:\n", $cookie_jar->as_string(), "\n";
}
print STDERR "load cookie jar from the file\n";
$cookie_jar->load();
$ua->cookie_jar($cookie_jar);
print STDERR "cookies loaded for update: \n", $cookie_jar->as_string(), "\n";
print STDERR "make update request\n";
#$req = POST($workurl, Content_Type => "xml", Content => $xml);
my $response = $ua->request(POST $workurl,
Content_Type => 'text/xml',
Content => $xml);

print $response->error_as_HTML unless $response->is_success;

print $response->as_string;
```


This script is a modification of the one above, but provides batch loading of XML files from a directory.  See [PURLBatchUploadingVersionOne](PURLBatchUploadingVersionOne.md) for details of the input format.

```
#!/usr/bin/perl -w

#use strict;

############################################################
#  sendbatchpurls.pl
#
#  David Wood (david@zepheira.com)
#  January 2010
#
#  Script to send PURLz v1.x batch loading XML files to a PURLz server.
#
#  Copyright 2010 Zepheira LLC.  Licensed under the Apache License,
#  Version 2.0 (the "License"); you may not use this file except in compliance
#  with the License. You may obtain a copy of the License at
#  http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
#  or agreed to in writing, software distributed under the License is distributed
#  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
#  express or implied. See the License for the specific language governing
#  permissions and limitations under the License.
#
###########################################################
use LWP::UserAgent;
use HTTP::Cookies;
use HTTP::Request::Common qw(GET PUT POST);

###############################
# CHANGE THESE VARS AS NEEDED #
###############################
my $server = 'localhost:8080';
my $userid = 'userid';
my $passwd = 'password';
my $authurl = "http://$server/admin/login/login-submit.bsh";
my $workurl = "http://$server/admin/purls";
my $cookiefile = 'lwpcookies.txt';
my $useoldfile = 0;
my $xml = "";
my $directory = "batch_files";

# set up user agent with a cookie jar
my $res;
my $req;
my $ua = LWP::UserAgent->new();
my $cookie_jar = HTTP::Cookies->new(
  file     => 'lwpcookies.txt',
  autosave => 1,
  ignore_discard => 1,
);
$ua->cookie_jar($cookie_jar);

print STDERR "no old cookie jar file, so asking for new authorization\n"
   if ($useoldfile && ! -e $cookiefile);

print STDERR "set up cookie jar\n";
unless ($useoldfile && -e $cookiefile) {
  # try the authorization call, saving the cookie
  $req = POST($authurl, [id       => $userid,
                        passwd   => $passwd,
                        referrer => '/docs/index.html',
  ]);
  print STDERR "make auth request\n";
  $res = $ua->request($req);
  print STDERR "save cookie jar file\n";
  $cookie_jar->save();
  print STDERR "auth cookies retrieved:\n", $cookie_jar->as_string(), "\n";
}
print STDERR "load cookie jar from the file\n";
$cookie_jar->load();
$ua->cookie_jar($cookie_jar);
print STDERR "cookies loaded for update: \n", $cookie_jar->as_string(), "\n";

# Open log file.
open LOG, ">purl_creations.log" or die "Couldn't open log file: $!\n";


# Make a new request for each file in the directory.
@files = <$directory/*> or die "ERROR: Can't open directory $directory for reading: $!\n";;
foreach $file (@files) {

  # Get content from file
  local $/=undef;
  open FILE, $file or die "Couldn't open file $file: $!\n";
  binmode FILE;
  $xml = <FILE>;
  close FILE;

  # DBG
  if ( $debug ) { print STDERR "make update request\n"; }

  my $response = $ua->request(POST $workurl,
    Content_Type => 'text/xml',
    Content => $xml);

  #print $response->error_as_HTML unless $response->is_success;

  # Report results.
  my $report = $response->as_string;
  if ( $report =~ m/<purl-batch total=\"50\"/ ) {
    print "$file OK\n";
    print LOG "$file OK\n";
  } else {
    print "\nERROR:  $file: $report\n\n";
    print LOG "\nERROR:  $file: $report\n\n";
  }
}

close(LOG);
```


### Common Lisp ###
This sample was provided by Alan Ruttenberg.  It was written using [ABCL](http://common-lisp.net/project/armedbear/) and uses libraries from [LSW](http://esw.w3.org/topic/LSW).

```

(defun login (host port user pass)
 (with-cookies-from
     (format nil
         "http://~a:~a/admin/login/login.bsh?referrer=/docs/index.html" host
         port)
   (let ((cookie (car *cookies*)))
     (multiple-value-bind (results)
	  (get-url  (format nil
                 "http://~a:~a/admin/login/login-submit.bsh?referrer=/docs/index.html"
                 host port)
		    :post `(("id" ,user) ("passwd" ,pass) ("referrer" "/docs/index.html"))
		    :force-refetch t :follow-redirects nil :head t)
	(if  (equalp (second (assoc "Location" results :test 'equalp))
             "/docs/index.html")
	     (values (second (assoc "Set-Cookie" results :test 'equalp)) cookie)
	     nil
	     ))
     )))

(defvar *purl-session* nil)

(defmacro with-purl-session ((host port login pass) &body body)
 `(let* ((*purl-session* (login ,host ,port ,login ,pass))
	  (*cookies* (list *purl-session*)))
    ,@body))


(defun add-purl (purl &optional (target "http://www.google.com/") (type "302"))
 (multiple-value-bind (body res)
     (get-url (format nil "http://127.0.0.1/admin/purl~a" purl)
	       :post
	       `(("type" ,type) ("maintainers" "test1") ("target" ,target) )
	       :appropriate-response
	       (lambda(res) (or (eql res 201) (eql res 409)))
	       )
   ;; fixme error handling is confusing
   (if (eql (second (assoc "response-code" res)) 409)
	nil
	(if (and body (#"matches" body "(?s).*<purl status=\"1\">.*"))
	    purl
	    nil))))

;;(with-purl-session ("127.0.0.1" 80 "test1" "test1")
;;	   (add-purl "/NET/obofo/" "http://www.obofoundry.org/" "partial"))

```


### Python ###
This simple example logs in as admin, creates a 410 purl, and validates that it exists.

```
import urllib, urllib2
opener = urllib2.build_opener(urllib2.HTTPCookieProcessor()) 
urllib2.install_opener(opener)
opener.open("http://localhost:8080/admin/login/login.bsh?referrer=/docs/index.html").read().close() # pull in the appropriate cookies
opener.open("http://localhost:8080/admin/login/login-submit.bsh",urllib.urlencode( (dict(id='admin', passwd='password')))).read().close() # login as admin
opener.open("http://localhost:8080/admin/purl/net/test2", urllib.urlencode(dict(type="410", maintainers="admin"))).read().close() # Create a 410 purl
opener.open("http://localhost:8080/admin/purl/net/test2").read().close() # resolve the metadata for the purl
opener.open("http://localhost:8080/net/test2").read().close() # resolve the purl itself
```

### VBScript ###
'''Code excerpt is provided by Chris Stockwell at Montana State Library.'''

```
'psuedocode for vbscript to create purls at purl.org
'open the table dbo.masterpurl … fields available are oclcnumber, purl, target, purlcreated, batch, bibtype
'loop
'  grab a purl from the database table were purlcreated = 0
'     assumes purls for latest batch have been added to the purl table
'  get a session cookie
'  POST the same Session cookie back to login and keep the session open
'  POST the new PURL to oclc purl.org
'  record the response from the purl server
'  note in database that purl was successfully uploaded 
‘end loop
'====================================================
CONST Batch = "circulation@mt.gov"
Dim PURLRESPONSEPATH
PURLRESPONSEPATH = "\\path where I store the purl response\" + BATCH + "\xmlresponsepurls6.xml"
'====================================================
'Set up the response path
'====================================================
Set fs = createobject("Scripting.FileSystemObject")
Set xmlresponsepurls6 = FS.CreateTextFile(PURLRESPONSEPATH)
Do While Not rs1.EOF
  '===========================================================
  'Get a session cookie
  '===========================================================
  pc = rs1.fields(3).value
  '1 is true ... the purl has already been created. False or 0, the purl has not been created
  if pc then
    'WScript.echo "pc is true " + CStr(pc)
  else
    'WScript.echo "pc is false " + CStr(pc)
    purlid = rs1.fields(1).value  'note purlid is really the entire purl beginning with http://purl.org
    purltarget = rs1.fields(2).value
    set sender = CreateObject("MSXML2.ServerXMLHTTP")
    sender.open "GET", "http://purl.org/admin/loginstatus"
    sender.send
    cookie = sender.getResponseHeader("Set-Cookie")
    set sender = Nothing
    '=============================================================
    'POST the same Session cookie back to login and keep the session open
    '=============================================================
    set sender = CreateObject("MSXML2.ServerXMLHTTP")
    sender.open "POST", "http://purl.org/admin/login/login-submit.bsh?id=jollyrog&passwd=f0psqnmv&"
    sender.setRequestHeader "SET-COOKIE", cookie
    sender.send 
    '=============================================================
    'when not commented out, the following code indicates I am logged in at this point
    '=============================================================
    'sender.open "GET", "http://purl.org/admin/loginstatus"
    'sender.send
    'WScript.echo sender.responseText
    '=============================================================
    'POST the PURL
    '=============================================================
    'strip off http://purl.org from each purl so purlid can be put into url for uploading to purl.org
    lenpurlid = len(purlid)
    purlid = right(purlid, (lenpurlid - 15))
    sender.open "POST", "http://purl.org/admin/purl" + purlid + "?type=302&maintainers=mslfix&target=" + purltarget
    sender.send
    '=============================================================
    'Record the response
    '=============================================================
    prehtml = sender.responseText
    xmlresponsepurls6.WriteLine prehtml + Chr(13)
    ...
Loop
prehtml = sender.responseText xmlresponsepurls6.WriteLine? prehtml + Chr(13) ... 
```


### C ###

This code sample was provided by Alan Bleasby of the European Bioinformatics Institute.

```

/* purlhack.c
**
** Usage: purlhack hostname username password xmlfile
**
** This program allows batch addition of PURLS from the xmlfile
** given as a parameter. It uses only basic GET and POST commands
** and server message retrieval. It was written as an exercise to
** determine the operation of the purl server.
**
** This program is free software; you can redistribute it and/or
** modify it under the terms of the GNU General Public License
** as published by the Free Software Foundation; either version 2
** of the License, or (at your option) any later version.
**
** This program is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU General Public License for more details.
**
*/

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <dirent.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

/*
** The PURL site does not accept urlencoded xml
** so don't do the next define
**
** #define DO_ENCODING
*/


static int connect_server(const char *hostname);
static int get_cookie(int sock, const char *hostname, const char *username,
                      const char *password, char *cookie);
static int read_xml(FILE *fp, char *xml);
static int xmlencode(char *xml, char *xmlenc);
static int send_xml(int sock, const char *hostname, char *cookie, char *xmlenc);
static int recv_buffer(int sock, char *buf);




#define SEND_STR(MSG) send(sock,MSG,strlen(MSG),0)


/* Maximum length of any response from server */
#define MAX_BUF_LEN 100000


static int connect_server(const char *hostname)
{
    struct sockaddr_in sin;
    int sock;
    struct hostent *host_addr;
    
    sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock == -1)
    {
        fprintf(stderr,"Error: Cannot open socket\n");
        return -1;
    }
    

    sin.sin_family = AF_INET;
    sin.sin_port = htons((unsigned short)80);

    host_addr = gethostbyname(hostname);

    if(!host_addr)
    {
        fprintf(stderr,"Can't find host [%s]\n",hostname);
        return -1;
    }

    sin.sin_addr.s_addr = *((int*)*host_addr->h_addr_list);


    if(connect(sock,(const struct sockaddr *)&sin,
                sizeof(struct sockaddr_in) ) == -1 )
    {
        fprintf(stderr,"Error: Cannot connect to host [%s]\n",hostname);
        return -1;
    }
    
    return sock;
}




static int get_cookie(int sock, const char *hostname, const char *username,
                      const char *password, char *cookie)
{
    char get[MAXNAMLEN];
    char *buf;
    char *start;
    char *end;

    
    sprintf(get,"GET /admin/login/login-submit.bsh?id=%s&passwd=%s "
            "HTTP/1.1\r\n"
            "User-Agent: Mozilla/4.0\r\n"
            "Host: %s\r\n"
            "Accept: *%c*\r\n\r\n",
            username,password,hostname,'/');

    if(SEND_STR(get) == -1)
    {
        fprintf(stderr,"Error: Cannot send GET [%s]\n",get);
        return -1;
    }


    buf = (char *) malloc(MAX_BUF_LEN);
    if(!buf)
    {
        fprintf(stderr,"Error: Cannot allocate cookie buffer\n");
        return -1;
    }

    if(recv_buffer(sock,buf))    
    {
        fprintf(stderr,"Error: Failure receiving cookie buffer\n");
        return -1;
    }


    start = strstr(buf,"Set-Cookie");
    if(!start)
    {
        fprintf(stderr,"Error: No cookie in the buffer\n");
        return -1;
    }
    end = start;
    while(*end >= ' ')
        ++end;
    *end = '\0';
    strcpy(cookie,start);

    free(buf);
    
    return 0;
}




static int read_xml(FILE *fp, char *xml)
{
    char line[MAXNAMLEN];
    char *p;
    int len;

    p = xml;
    
    
    while(fgets(line,MAXNAMLEN,fp))
    {
        len = strlen(line);
        strcpy(p,line);
        p += len;
        *p = '\0';
    }
    
    return 0;
}




static int xmlencode(char *xml, char *xmlenc)
{
#ifdef DO_ENCODING
    char *p = NULL;
    char *q = NULL;
    char c;
    static const char *excl="$&+,/:;=?@_-.";

    p = xml;
    q = xmlenc;
    
    while((c = *p))
    {
        if(strchr(excl,(int)c))
            *q++ = c;
        else if((c>='A' && c <='Z') || (c>='a' && c<='z') || (c>='0' && c<='9'))
            *q++ = c;
        else
        {
            sprintf(q,"%%%.2x",(int)c);
            q += 3;
        }
        
        ++p;
    }

    *q = '\0';
#else
    strcpy(xmlenc,xml);
#endif
    
    return 0;
}




static int send_xml(int sock, const char *hostname, char *cookie, char *xmlenc)
{
    char hcookie[MAXNAMLEN];
    char *p;
    char putstr[MAXNAMLEN];
    
    p = strstr(cookie,"NETKERNELSESSION");
    if(!p)
    {
        fprintf(stderr,"Error: Cannot find NETKERNELSESSION\n");
        return -1;
    }

    sprintf(hcookie,"Cookie: %s",p);

    hcookie[strlen(hcookie)]='\0';
    

    sprintf(putstr,"POST /admin/purls "
            "HTTP/1.1\r\n"
            "User-Agent: Mozilla/4.0\r\n"
            "Host: %s\r\n"
            "Accept: */*\r\n"
            "%s\r\n"
            "Content-Length: %d\r\n"
            "Content-Type: text/xml\r\n"
            "\r\n",
            hostname,hcookie,(int)strlen(xmlenc));

    if(SEND_STR(putstr) == -1)
    {
        fprintf(stderr,"Error: Cannot send POST [%s]\n",putstr);
        return -1;
    }

    if(SEND_STR(xmlenc) == -1)
    {
        fprintf(stderr,"Error: Cannot send xml string [%s]\n",xmlenc);
        return -1;
    }

    if(SEND_STR("\r\n") == -1)
    {
        fprintf(stderr,"Error: Cannot send terminating string\n");
        return -1;
    }


    return 0;
}




static int recv_buffer(int sock, char *buf)
{
    int len;
    struct sockaddr saddr;
    socklen_t addrlen;

    addrlen = sizeof(saddr);

    len = recvfrom(sock, (void *)buf, MAX_BUF_LEN, 0,
                   &saddr, &addrlen);
    buf[len] = '\0';

    return 0;
}




int main(int argc, char **argv)
{
    int sock;
    const char *hostname;
    const char *username;
    const char *password;

    const char *xmlfile;
    char cookie[MAXNAMLEN];
    char *xml    = NULL;
    char *xmlenc = NULL;
    char *buf    = NULL;
    
    FILE *xfp;
    struct stat sb;
    
    if(argc != 5)
    {
        fprintf(stderr,"Usage: purlhack hostname username password xmlfile\n");
        exit(-1);
    }


    hostname = argv[1];
    username = argv[2];
    password = argv[3];
    xmlfile  = argv[4];

    
    if(stat(xmlfile,&sb) == -1)
    {
        fprintf(stderr,"Error: Cannot stat file [%s]\n",xmlfile);
        return -1;
    }

    
    xml    = (char *) malloc(sb.st_size + 1);
    xmlenc = (char *) malloc((sb.st_size * 3) + 1);

    if(!xml || !xmlenc)
    {
        fprintf(stderr,"Error: insufficient memory\n");
        return -1;
    }
    
    
    fprintf(stdout,"Connecting to host %s\n",argv[1]);

    sock = connect_server(hostname);
    if(sock == -1)
        exit(-1);

    if(get_cookie(sock, hostname, username, password, cookie) == -1)
        exit(-1);

    xfp = fopen(xmlfile,"r");
    if(!xfp)
    {
        fprintf(stderr,"Error: Cannot open xml file [%s]\n",xmlfile);
        return -1;
    }

    /*
    ** Close the current connection and open another for the POST
    */
    
    close(sock);

    sock = connect_server(hostname);
    if(sock == -1)
        exit(-1);
  
    
    read_xml(xfp,xml);
    xmlencode(xml,xmlenc);

    if(send_xml(sock,hostname,cookie,xmlenc) == -1)
    {
        fprintf(stderr,"Error: In POSTing xml to server\n");
        return -1;
    }
    

    /* Print any return from the server */

    buf = (char *) malloc(MAX_BUF_LEN);
    if(!buf)
    {
        fprintf(stderr,"Error: Cannot server buffer\n");
        return -1;
    }

    if(recv_buffer(sock,buf))    
    {
        fprintf(stderr,"Error: Failure receiving server buffer\n");
        return -1;
    }


    fprintf(stdout,"\n\nServer output-----------\n");
    fprintf(stdout,"%s\n\n",buf);

    close(sock);
    
    free(buf);
    free(xml);
    free(xmlenc);
    fclose(xfp);
    
    return 0;
}
```