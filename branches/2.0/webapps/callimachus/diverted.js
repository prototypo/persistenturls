// diverted.js

if (window.attachEvent) {
	window.attachEvent("onload", divertLinks)
} else {
	window.addEventListener("DOMContentLoaded", divertLinks, false)
}

function divertLinks() {
	var links = document.getElementsByTagName("a")
	for (var i=0; i<links.length; i++) {
		var link = links.item(i)
		var css = link.className
		if (css && css.match(/\bdiverted\b/)) {
			if (link.attachEvent) {
				link.attachEvent("onclick", divertLink(link))
			} else {
				link.addEventListener("click", divertLink(link), false)
			}
		}
	}
}

function divertLink(link) {
	return function(e) {
		if (link.href) {
			setTimeout(function() { location = diverted(link.href, link) }, 0)
			return false;
		} else {
			return true
		}
	}
}

function diverted(url, node) {
	var prefix = location.protocol + '//' + location.host + '/callimachus/diverted;'
	if (url.indexOf(':') < 0) {
		if (node.baseURIObject && node.baseURIObject.resolve) {
			url = node.baseURIObject.resolve(url)
		} else {
			var a = document.createElement("a")
			a.setAttribute("href", url)
			if (a.href) {
				url = a.href
			}
		}
	}
	if (url.indexOf('?') > 0) {
		var uri = url.substring(0, url.indexOf('?'))
		var qs = url.substring(url.indexOf('?'))
		return prefix + encodeURIComponent(uri) + qs
	}
	return prefix + encodeURIComponent(url)
}