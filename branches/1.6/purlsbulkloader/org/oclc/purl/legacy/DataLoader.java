package org.oclc.purl.legacy;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class DataLoader {
    public static final String USER_FILE = "passwd.gz",
            GROUP_FILE = "group.gz",
            DOMAIN_INI_FILE = "domain.ini.gz",
            DOMAIN_FILE = "domain.gz",
            PURL_FILE = "main.list.gz",
            SORTED_USERNAMES = "users.txt",
            SORTED_GROUPNAMES = "groupnames.txt";

    private static PURLClient client = new PURLClient();
    private static String host = "localhost";
    private static String port = "8080";
    private static String admin = "admin";
    private static String adminPassword = "password";
    private static int purlsPerBatch = 50;
    private static boolean loadUsers = true;
    private static boolean loadGroups = true;
    private static boolean loadDomains = true;

    private static final Set<Pattern> purlIgnorePatterns = new HashSet<Pattern>();

    private static final Set<String> seenUsersSet = new HashSet<String>();
    private static final Set<String> seenGroupsSet = new HashSet<String>();
    private static Set<String> seenDomainSet = new HashSet<String>();

    public static void main(String[] args) throws Exception  {
        // Load properties

        InputStream in = DataLoader.class.getResourceAsStream("DataLoader.properties");
        if (in != null) {
            Properties prop = new Properties();
            prop.load(in);
            parseProperties(prop);
        }

        if (args.length != 1 && args.length != 5) {
            //error("Expecting single directory indicating root directory containing legacy data.");
            DataLoader.usage();
            System.exit(-1);
        }
        for (int inx = 0 ; inx < args.length ; inx++) {
            if (args[inx].equals("-h")) {
                inx++;
                host = args[inx];
            } else if (args[inx].equals("-p")) {
                inx++;
                port = args[inx];
            }
        }
        File rootDir = new File(args[args.length -1]);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            error("Directory: " + rootDir.getAbsolutePath()
                    + " does not exist or is not a directory.");
            System.exit(-2);
        }

        // TODO: Parameterize this
        login(admin, adminPassword);
        if (loadUsers) {
            loadUsers(rootDir);
        }
        if (loadGroups) {
            loadGroups(rootDir);
        }
        if (loadDomains) {
            loadDomains(rootDir);
        }
        loadPURLs(rootDir);

    }

    private static void parseProperties(Properties props) {
         for (Enumeration e = props.propertyNames() ; e.hasMoreElements() ;) {
             String key = (String)e.nextElement();
             if ("host".equals(key)) {
                 host = props.getProperty(key);
             } else if ("port".equals(key)) {
                 port = props.getProperty(key);
             } else if ("admin.username".equals(key)) {
                 admin = props.getProperty(key);
             } else if ("admin.password".equals(key)) {
                 adminPassword = props.getProperty(key);
             } else if (key.startsWith("purl.ignore")) {
                 purlIgnorePatterns.add(Pattern.compile(props.getProperty(key)));
             } else if ("user.load".equals("key")) {
                 loadUsers = Boolean.getBoolean(props.getProperty(key));
             } else if ("group.load".equals("key")) {
                 loadGroups = Boolean.getBoolean(props.getProperty(key));
             } else if ("domain.load".equals("key")) {
                 loadDomains = Boolean.getBoolean(props.getProperty(key));
             }
         }
    }

    private static void login(String username, String password)
            throws IOException {
        String url = "http://" + host + ":" + port
                + "/admin/login/login-submit.bsh";

        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("id", username);
        formParameters.put("passwd", password);
        formParameters.put("referrer", "/docs/index.html");

        client.login(url, formParameters);
    }

    private static String registerUser(String uid, String name, String password,
                                       String email, String affiliation, String hint, String justification)
            throws IOException {

        String url = "http://" + host + ":" + port + "/admin/user/" + uid;

        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("name", name);
        formParameters.put("affiliation", affiliation);
        formParameters.put("email", email);
        formParameters.put("passwd", "des:" + password);
        formParameters.put("hint", URLEncoder.encode(hint, "UTF-8"));
        formParameters.put("justification", URLEncoder.encode(justification, "UTF-8"));

        return client.registerUser(url, formParameters);

    }

    private static String registerGroup(String gid, String maintainers,
                                        String members, String comments) throws IOException {
        String url = "http://" + host + ":" + port + "/admin/group/" + gid;

        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("maintainers", maintainers);
        formParameters.put("members", members);
        formParameters.put("name", gid);
        formParameters.put("comments", comments);

        String result = client.createGroup(url, formParameters);
        System.out.println(result);
        return result;
    }

    private static String registerDomain(String did, boolean isPublic,
                                         String maintainers, String members, String comments)
            throws IOException {
        String url = "http://" + host + ":" + port + "/admin/domain" + did;

        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("maintainers", maintainers);
        formParameters.put("writers", members);
        formParameters.put("name", did);
        formParameters.put("comments", comments);
        formParameters.put("public", Boolean.toString(isPublic));

        String result = client.createDomain(url, formParameters);
        return result;
    }

    private static boolean validatePURL(String purl) {
        try {
            new URL("http://localhost/purl/" + purl);
            if (!purl.contains("&") && !purl.contains("$")) {
                return true;
            }
        } catch (MalformedURLException meo) {

        }
        return false;
    }

    private static String createPURL(BufferedWriter bw, String purl,
                                     String type, String url, String maintainer) throws IOException {
        String retValue = null;

        if ((purl != null) && (type != null)
                && (maintainer != null)) {

            StringBuffer sb = new StringBuffer();
            if (!validatePURL(purl)) {
                bw.append("Ignoring PURL: " + purl
                        + " because it does not result in a valid URL\n");
                return null;
            }
            if (url != null) {
                try {
                    url = cleanseTargetURL(url);
                } catch (IllegalStateException ise) {
                    bw.append("Creating 410 PURL: " + purl
                            + " because of bogus target URL : " + url);
                    bw.append("\n");
                    type = "410";
                }

                url = url.replaceAll("&", "&amp;");
                url = url.replaceAll("'", "&apos;");
                url = url.replaceAll("\"", "&quot;");
                url = url.replaceAll("<", "&lt;");
                url = url.replaceAll(">", "&gt;");
            } else if (!"410".equals(type)) {
                bw.append("Creating 410 PURL: " + purl
                            + " because of missing URL ");
                    bw.append("\n");
                type="410";
            }

            purl = purl.replaceAll("'", "&apos;");
            sb.append("<purl id=\"");
            sb.append(purl);
            sb.append("\" type=\"");
            sb.append(type);
            sb.append("\">\n");
            sb.append("<maintainers>\n");
            StringTokenizer st = new StringTokenizer(maintainer, " ", false);

            boolean handledAdminSubstitution = false;

            while (st.hasMoreTokens()) {
                String m = st.nextToken();
                if (seenUsersSet.contains(m.toUpperCase())) {
                    sb.append("<uid>");
                    sb.append(m);
                    sb.append("</uid>\n");
                } else if (seenGroupsSet.contains(m.toUpperCase())) {
                    sb.append("<gid>");
                    sb.append(m);
                    sb.append("</gid>\n");
                } else {
                    // We don't want duplicate admin substitutions
                    if (!handledAdminSubstitution) {
                        sb.append("<uid>admin</uid>\n");
                        handledAdminSubstitution = true;
                    }
                }
            }
            sb.append("</maintainers>\n");

            if (type.equals("302") || type.equals("partial")) {
                sb.append("<target url=\"");
                sb.append(url);
                sb.append("\"/>\n");

            } else {
            }
            sb.append("</purl>\n");
            retValue = sb.toString();
        }

        return retValue;
    }

    private static String registerPURLS(String purls) throws IOException {
        String url = "http://" + host + ":" + port + "/admin/purls/";
        return client.createPurls(url, purls);
    }

    private static Set<String> parseUsers(File users) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(users));
        String line;
        Set<String> results = new HashSet<String>();
        while ((line = br.readLine()) != null) {
            results.add(line.trim().toUpperCase());
        }
        return results;

    }

    private static void loadPURLs(File rootDir) {

        BufferedWriter bw = null;
        try {
            File purlLogFile = new File(rootDir, "purl-"
                    + System.currentTimeMillis() + ".log");
            bw = new BufferedWriter(new FileWriter(purlLogFile));

            try {
                if (seenUsersSet.size() == 0) {
                    seenUsersSet.addAll(parseUsers(new File(rootDir, SORTED_USERNAMES)));
                }
                if (seenGroupsSet.size() == 0) {
                    seenGroupsSet.addAll(parseUsers(new File(rootDir, SORTED_GROUPNAMES)));
                }

            } catch (Exception e1) {
                bw.write("Invalid users/groups file");
                return;
            }

            BufferedReader br = getReader(rootDir, PURL_FILE);

            String line = null;
            int incompleteCount = 0;
            int ignoredCount = 0;
            int goneCount = 0;
            int unprocessedCount = 0;
            int successes = 0;
            int failures = 0;
            int failedBatchCount = 0;

            // TODO: Externalize this to some external representation
            Pattern purlPattern = Pattern.compile("<purl>(.*?)</purl>");
            Pattern modifyPattern = Pattern.compile("<modify>(.*?)</modify>");
            Pattern createPattern = Pattern.compile("<create>(.*?)</create>");
            Pattern urlPattern = Pattern.compile("<url>(.*?)</url>");
            Pattern idPattern = Pattern.compile("<id>(.*?)</id>");
            Pattern partialPattern = Pattern.compile("<partial></partial>");

            List<String> purlList = new ArrayList<String>();

            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("#")) {
                    continue;
                }

                // Find the next PURL
                Matcher matched = purlPattern.matcher(line);

                if (matched.find()) {
                    String purl = extractGroupMatch(line, matched, 1);

                    boolean ignored = false;
                    for (Pattern p : purlIgnorePatterns) {
                        Matcher m = p.matcher(purl);
                        if (m.find()) {
                            ignoredCount++;
                            ignored = true;
                            continue;
                        }
                    }
                    if (ignored) {
                        continue;
                    }

                    String modify = null;
                    String create = null;
                    String url = null;
                    String id = null;
                    String type = null;

                    try {
                        // Read the modify line
                        line = readExpectedLine(br);
                        Matcher modifyMatched = modifyPattern.matcher(line);
                        if (modifyMatched.find()) {
                            modify = extractGroupMatch(line, modifyMatched, 1);
                        }
                        // Read the create line
                        line = readExpectedLine(br);
                        Matcher createMatched = createPattern.matcher(line);
                        if (createMatched.find()) {
                            create = extractGroupMatch(line, createMatched, 1);
                        }

                        // Read the URL or Type line
                        line = readExpectedLine(br);

                        Matcher partialMatched = partialPattern.matcher(line);

                        if (partialMatched.find()) {
                            type = "partial";
                            // Read the URL line
                            line = readExpectedLine(br);
                        }

                        Matcher urlMatched = urlPattern.matcher(line);
                        if (urlMatched.find()) {
                            url = extractGroupMatch(line, urlMatched, 1);
                        }

                        // Read the id line
                        line = readExpectedLine(br);
                        Matcher idMatched = idPattern.matcher(line);
                        if (idMatched.find()) {
                            id = extractGroupMatch(line, idMatched, 1);
                        }
                        if (id == null) {
                            id = "ADMIN";
                        }
                        if (purl != null) {
                            if (url == null) {
                                type = "410";
                                bw.append("Creating 410 PURL: Incomplete: " + purl);
                                bw.append("\n");
                            }
                            if (type == null) {
                                type = "302";
                            }
                            String purlString = createPURL(bw, purl, type,
                                    url, id);
                            if (purlString != null) {
                                if (purlString.contains("type=\"410\"")) {
                                    goneCount++;
                                }
                                purlList.add(purlString);
                            } else {
                                unprocessedCount++;
                            }

                        } else {
                            bw.append("Incomplete: " + purl);
                            bw.append("\n");
                            unprocessedCount++;
                        }

                        if (purlList.size() == purlsPerBatch) {
                            int size = purlList.size();
                            String result = createPurls(purlList);
                            if (!result.startsWith("<purl-batch")) {
                                failedBatchCount += size;
                            } else {
                                successes += parseAttribute(result, "numCreated");
                                failures += parseAttribute(result, "failed");
                            }
                            bw.append(result);
                            bw.append("\n");
                        }

                        // TODO: Don't skip over the history
                        // continue;

                    } catch (IllegalStateException ise) {
                        bw.append("Error processing: " + purl);
                        bw.append("\n");
                        continue;
                    } catch (Throwable t) {
                        bw.append("Error processing: " + t.getMessage());
                        bw.append("\n");
                        continue;
                    }
                }
                bw.flush();
            }
            if (purlList.size() > 0) {
                int size = purlList.size();
                String result = createPurls(purlList);
                if (!result.startsWith("<purl-batch")) {
                    failedBatchCount += size;
                } else {
                    successes += parseAttribute(result, "numCreated");
                    failures += parseAttribute(result, "failed");
                }
                bw.append(result);
                bw.append("\n");
            }

            bw.append("\nRegistered PURLS: " + successes);
            bw.append("\nFailed PURLS: " + failures);
            bw.append("\nIncomplete PURLS: " + incompleteCount);
            bw.append("\nIgnored ECO PURLS: " + ignoredCount);
            bw.append("\nUnprocessed PURLS: " + unprocessedCount);
            bw.append("\nFailed Batch PURLS: " + failedBatchCount);
            bw.append("\nTotal: " + (successes + failures + incompleteCount + ignoredCount + unprocessedCount + failedBatchCount));
            bw.append("\n");

            bw.append("\nRegistered 410 PURLs: " + goneCount);
            bw.append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private static int parseAttribute(String input, String attr) {
        String start = input.substring(input.indexOf(attr) + (attr + "=\"").length());
        return Integer.parseInt(start.substring(0, start.indexOf('"')));

    }

    private static String createPurls(List<String> purlList) throws IOException {
        StringBuffer sb = new StringBuffer("<purls>");
        for (String s : purlList) {
            sb.append(s);
        }
        sb.append("</purls>");

        try {
            String result = registerPURLS(sb.toString());
            purlList.clear();
            if (!result.startsWith("<purl-batch")) {
                System.out.println(result);
                return "<invalid-batch>" + sb.toString() + "</invalid-batch>";
            }
            return result;
        } catch (Throwable t) {
            t.printStackTrace();
            return "<invalid-batch>" + sb.toString() + "</invalid-batch>";

        }
    }

    private static String cleanseTargetURL(String origURL)
            throws IllegalStateException {
        String url = origURL;

        // There is a problem in the current data extraction that pulls
        // bogus data out of the database.
        if ((url.indexOf(0x07) > 0) || (url.indexOf(0x01d) > 0) || (url.indexOf(0x01b) > 0) || (url.indexOf(0x2) > 0)) {
            throw new IllegalStateException("Invalid bytes in target URL");

        }

        // There is some bogus data here with a period followed by one or more
        // spaces
        while (url.contains(". ")) {
            url = url.replace(". ", ".");
        }

        if (url.contains(" htm")) {
            url = url.replace(" htm", ".htm");
        }

        url = url.replaceAll("\\\\", "/");
        url = url.replaceAll("\"", "&quot;");
        // Fix sloppy target URLs so that they validate
        if (!url.startsWith("http://")
                && !url.startsWith("gopher:")
                && !url.startsWith("telnet:")
                && !url.startsWith("mailto:")
                && !url.startsWith("javascript:")
                && !url.startsWith("ftp:")) {
            // There are some that start with http:, but not http://
            if (url.startsWith("http:")) {
                url = url.substring(5);
            } else if (url.startsWith("htttp:://")) {
                // At least one of these too
                url = url.substring(9);
            }
            url = "http://" + url;
        }

        return url;
    }

    private static void loadDomains(File rootDir) {

        BufferedWriter bw = null;

        try {
            BufferedReader br = getReader(rootDir, DOMAIN_FILE);
            File domainLogFile = new File(rootDir, "domain-"
                    + System.currentTimeMillis() + ".log");
            bw = new BufferedWriter(new FileWriter(domainLogFile));

            try {
                if (seenUsersSet.size() == 0) {
                    seenUsersSet.addAll(parseUsers(new File(rootDir, SORTED_USERNAMES)));
                }
                if (seenGroupsSet.size() == 0) {
                    seenGroupsSet.addAll(parseUsers(new File(rootDir, SORTED_GROUPNAMES)));
                }

            } catch (Exception e1) {
                bw.write("Invalid users/groups file");
                return;
            }

            String line;
            String did;
            String maintainers;
            String members;
            String comments = "";
            String result;

            Map<String, List<String>> unresolveds = new HashMap<String, List<String>>();
            Set<String> incomplete = new HashSet<String>();

            int unresolvedCount = 0;
            int duplicateCount = 0;
            // Note: This will probably break on *REALLY* large
            // domain files, but I don't think in practice that
            // should be a problem.

            List<String> lines = new ArrayList<String>();
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("#")) {
                    continue;
                }

                if (line.startsWith("/")) {
                    lines.add(line);
                } else {
                    String last = lines.remove(lines.size() - 1);
                    lines.add(last + line);
                }
            }

            Collections.sort(lines, Collections.reverseOrder());

            Iterator<String> lineItor = lines.iterator();

            while (lineItor.hasNext()) {
                line = lineItor.next();

                // Clean up any errant whitespace
                String[] parts = line.split(":");
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = parts[i].trim();
                }

                did = parts[0];

                if (seenDomainSet.contains(did)) {
                    duplicateCount++;
                    continue;
                }

                checkSuperDomains(did);

                UserValidator uv = new UserValidator() {

                    public boolean valid(String member) {
                        return seenUsersSet.contains(member.toUpperCase())
                                || seenGroupsSet.contains(member.toUpperCase());
                    }

                };

                maintainers = parts[1].replaceAll(" ", ",");
                maintainers = UserHelper.generateValidUserList(maintainers, uv);

                if (parts.length > 2) {
                    members = parts[2].replaceAll(" ", ",");
                    members = UserHelper.generateValidUserList(members, uv);
                } else {
                    members = maintainers;
                }

                if (parts.length > 3) {
                    comments = parts[3];
                }

                boolean unresolved = false;
                int unresolvedMaintainers = 0;
                int unresolvedMembers = 0;
                int totalMaintainers = 0;
                int totalMembers = 0;

                StringTokenizer st = new StringTokenizer(maintainers, ",");
                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    totalMaintainers++;
                    if (!seenUsersSet.contains(s.toUpperCase()) && !seenGroupsSet.contains(s.toUpperCase())) {
                        System.out.println("We haven't yet seen: " + s);
                        unresolved = true;
                        unresolvedMaintainers++;

                        List<String> l = unresolveds.get(did);
                        if (l == null) {
                            l = new ArrayList<String>();
                            unresolveds.put(did, l);
                        }

                        // Don't double-add
                        if (!l.contains(s)) {
                            l.add(s);
                        }
                    }
                }

                st = new StringTokenizer(members, ",");
                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    totalMembers++;
                    if (!seenUsersSet.contains(s.toUpperCase()) && !seenGroupsSet.contains(s.toUpperCase())) {
                        System.out.println("We haven't yet seen: " + s);
                        unresolved = true;
                        unresolvedMembers++;

                        List<String> l = unresolveds.get(did);
                        if (l == null) {
                            l = new ArrayList<String>();
                            unresolveds.put(did, l);
                        }

                        // Don't double-add
                        if (!l.contains(s)) {
                            l.add(s);
                        }
                    }
                }

                // TODO: If Group is completely defined, go ahead and create it
                // otherwise store it and check later

                int missingMembers = totalMembers - unresolvedMembers;
                int missingMaintainers = totalMaintainers
                        - unresolvedMaintainers;

                if (!unresolved) {
                    try {
                        result = registerDomain(did, false, maintainers,
                                members, comments);
                        if (result.startsWith("<domain status=\"1\">")) {
                            seenDomainSet.add(did);
                            bw.append("Created domain: " + did);
                            bw.append("\n");
                        } else {
                            bw.append(result);
                            bw.append("\n");
                            unresolvedCount++;

                            incomplete.add(did);
                        }

//                        bw.append("result for " + did + ": " + result);
//                        bw.append("\n");
                    } catch (Throwable t) {
                        bw.append("Error creating domain: " + did + ":"
                                + t.getMessage());
                        bw.append("\n");
                        unresolvedCount++;
                        incomplete.add(did);
                    }
                } else {
                    System.out.println(did + " has some unresolved elements");
                    boolean incompl = false;

                    if (((totalMembers - unresolvedMembers) > 0)
                            && ((totalMaintainers - unresolvedMaintainers) > 0)) {
                        System.out.println(did
                                + " is incomplete but creatable.");
                        incomplete.add(did);
                        incompl = true;
                    }

                    List<String> l = unresolveds.get(did);
                    StringBuffer sb = new StringBuffer(did);

                    if (incompl) {
                        sb.append(":I");
                    } else {
                        sb.append(":U");
                    }

                    sb.append(":");
                    for (int i = 0; i < l.size(); i++) {
                        sb.append(l.get(i));
                        if (i < l.size() - 1) {
                            sb.append(",");
                        }
                    }
                    sb.append("\n");
                    bw.write(sb.toString());

                    /*
                          * if(did.startsWith("/DC")) { System.out.println(members); }
                          */

                    unresolvedCount++;
                }
            }

            bw.append("\nRegistered this many domains: "
                    + seenDomainSet.size());
            bw.append("\nCould register these additional domains: "
                    + incomplete.size());
            bw.append("\nDuplicate domains: " + duplicateCount);
            bw.append("\nNumber of unregisterable domains: "
                    + unresolvedCount);
            bw.append("\nUnresolved:");
            for (String s : incomplete) {
                bw.append("\t" + s + "\n");
                // System.out.println(s);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                bw.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

    }

    private static boolean registerGroup(Map<String, String[]> groupMap,
                                         String group) {

        boolean retValue = false;
        String[] groupData = groupMap.get(group);

        if (groupData != null) {
            String gid = groupData[0];
            String owner = groupData[1];
            String maintainers = null;
            String members = null;
            String comments = "";

            maintainers = owner.replaceAll(" ", ",");

            // normalize the maintainers
            maintainers = normalizeOwners(gid, groupMap, maintainers);
            StringTokenizer st;
            if (groupData.length >= 3) {
                members = groupData[2].replaceAll(" ", ",");
            } else {
                members = owner;
            }

            if (groupData.length == 4) {
                comments = groupData[3];
            }

            System.out.println("-----------------");
            System.out.println(gid);
            System.out.println(owner);
            members = normalizeOwners(gid, groupMap, members);
            // TODO: If Group is completely defined, go ahead and create it
            // otherwise store it and check later
            try {
                registerGroup(gid, maintainers, members, comments);
                retValue = true;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (retValue) {
            seenGroupsSet.add(group);
        }

        return retValue;
    }

    private static String normalizeOwners(String gid, Map<String, String[]> groupMap, String maintainers) {
        String finalMaintainers = "";
        StringTokenizer st = new StringTokenizer(maintainers, ",");

        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            System.out.println("group: " + gid);
            System.out.println("maintainer: " + s);
            if (!s.equals(gid)) {
                if (!seenUsersSet.contains(s.toUpperCase())
                        && !seenGroupsSet.contains(s.toUpperCase())) {
                    if (!registerGroup(groupMap, s)) {
                        System.out.println(gid + " has some unresolved elements");
                        continue;
                    }
                }
                if (finalMaintainers.length() > 0) {
                    finalMaintainers += ",";
                }
                finalMaintainers += s;
            }
        }

        maintainers = finalMaintainers;
        if (maintainers.length() == 0) {
            maintainers = "admin";
        }

        return maintainers;
    }

    private static void loadGroups(File rootDir) {
        BufferedWriter bw = null;
        int lineCount = 0;

        try {
            BufferedReader br = getReader(rootDir, GROUP_FILE, "ISO-8859-1");
            File groupLogFile = new File(rootDir, "group-"
                    + System.currentTimeMillis() + ".log");
            bw = new BufferedWriter(new FileWriter(groupLogFile));

            try {
                if (seenUsersSet.size() == 0) {
                    seenUsersSet.addAll(parseUsers(new File(rootDir, SORTED_USERNAMES)));
                }

            } catch (Exception e1) {
                bw.write("Invalid users file");
                return;
            }

            String line = null;
            String[] parts = null;
            String gid = null;

            Map<String, String[]> unhandledMap = new HashMap<String, String[]>();

            // Read all of the lines in, we may need to make multiple
            // passes to find forward
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("#")) {
                    continue;
                }

                lineCount++;

                parts = line.split(":");

                if (unhandledMap.containsKey(parts[0])) {
                    System.out.println("Ignoring duplicate record for group: "
                            + parts[0]);
                } else {
                    unhandledMap.put(parts[0], parts);
                }
            }

            Iterator<String> groupItor = unhandledMap.keySet().iterator();

            while (groupItor.hasNext()) {
                gid = groupItor.next();

                if (!seenGroupsSet.contains(gid)) {
                    parts = unhandledMap.get(gid);

                    if (registerGroup(unhandledMap, gid)) {
                        System.out.println("Registered: " + gid);
                    }
                }
            }

            Iterator<String> unresolvedGroupsItor = unhandledMap.keySet()
                    .iterator();
            while (unresolvedGroupsItor.hasNext()) {
                String next = unresolvedGroupsItor.next();
                if (!seenGroupsSet.contains(next)) {
                    System.out.println("\nUnresolved Group: " + next);
                }
            }
            bw.append("Total groups: " + lineCount);
            bw.append("\n");
            bw.append("Registered groups: " + seenGroupsSet.size());
            bw.append("\n");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                bw.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private static void loadUsers(File rootDir) throws IOException {

        BufferedWriter bw = null;

        try {
            BufferedReader br = getReader(rootDir, USER_FILE);
            int lineCount = 0;
            File userLogFile = new File(rootDir, "user-"
                    + System.currentTimeMillis() + ".log");
            bw = new BufferedWriter(new FileWriter(userLogFile));
            String line = null;
            String[] parts = null;
            String uid = null;
            String password = null;
            String name = null;
            String email = "";
            String affiliation = "";
            String hint = "";

            seenUsersSet.add("admin".toUpperCase());

            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("#")) {
                    continue;
                }

                lineCount++;

                parts = line.split(":");
                // Clean up any errant whitespace
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = parts[i].trim();
                }

                uid = parts[0];
                password = parts[1];
                name = parts[2];

                if (parts.length >= 4) {
                    email = parts[3];
                }

                if (parts.length >= 5) {
                    affiliation = parts[4];
                }

                if (parts.length == 6) {
                    hint = parts[5];
                }

                String result = registerUser(uid, name, password, email, affiliation, hint,
                        "Legacy User");
                if (result.startsWith("<user")) {
                    seenUsersSet.add(uid.toUpperCase());
                } else {
                    bw.append("Failed to add " + uid + ".  Trying again.\n");
                    result = registerUser(uid, name, password, "", "", "", "Legacy User");
                    bw.append(result + "\n");
                    if (result.startsWith("<user")) {
                        seenUsersSet.add(uid.toUpperCase());
                    }
                    System.out.println(result);
                }


            }

            // admin is auto-added so we don't count that
            bw.append("Total users: " + (lineCount - 1));
            bw.append("\n");
            bw.append("Registered users: " + seenUsersSet.size());
            bw.append("\n");

        } finally {
            bw.close();
        }
    }

    public static BufferedReader getReader(File rootDir, String file) {
        return getReader(rootDir, file, "UTF-8");
    }

    public static BufferedReader getReader(File rootDir, String file,
                                           String charSet) {
        BufferedReader retValue = null;
        InputStream is = null;
        File dataFile = new File(rootDir, file);

        try {
            is = new FileInputStream(dataFile);

            if (dataFile.getName().endsWith(".gz")
                    || dataFile.getName().endsWith(".gzip")) {
                is = new GZIPInputStream(is);
            }

            retValue = new BufferedReader(new InputStreamReader(is, charSet));
        } catch (FileNotFoundException e) {
            error("Cannot read user file: " + dataFile.getAbsolutePath());
        } catch (IOException e) {
            error("Error reading user file: " + dataFile.getAbsolutePath());
        }

        return retValue;
    }

    private static void checkSuperDomains(String domain) throws IOException {
        String[] domainElements = domain.substring(1).split("/");

        if (domainElements.length == 1) {
            return;
        }

        StringBuffer superDomains = new StringBuffer();

        for (int i = 0; i < domainElements.length - 1; i++) {
            superDomains.append("/");
            superDomains.append(domainElements[i]);

            if (!superDomains.toString().equals(domain)) {
                String s = superDomains.toString();
                if (!seenDomainSet.contains(s)) {
                    System.out.println("I haven't yet seen: " + s);
                    // registerDomain(s, true, "admin", "admin", "Auto-Created
                    // Public Domain");
                    seenDomainSet.add(s);
                    // registerDomain();
                }
            }
        }
    }

    private static void error(String message) {
        System.out.println("Error: " + message);
    }

    public static void usage() {
        System.out.println("Usage: java " + DataLoader.class.getCanonicalName()
                + "[-h <host> -p <port>] <legacy-data-dir>");
    }

    private static String readExpectedLine(BufferedReader br)
            throws IllegalStateException, IOException {
        String retValue = br.readLine();
        if (retValue == null) {
            throw new IllegalStateException();
        }
        return retValue;
    }

    private static String extractGroupMatch(String line, Matcher m, int group) {
        return line.substring(m.start(group), m.end(group));
    }
}
