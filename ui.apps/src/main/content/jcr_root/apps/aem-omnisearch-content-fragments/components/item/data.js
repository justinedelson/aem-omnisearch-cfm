/*global org, com, java*/
var CONTENT_FRAGMENT_EDITOR_VANITY = "/editor.html";
var CONTENT_FRAGMENT_PROPERTIES_PATH = "/mnt/overlay/dam/cfm/admin/content/metadata-editor.html";

use(function() {
    var asset, lastModified, rtf, formattedRelativeTime, escapedPath;

    asset = resource.adaptTo(com.day.cq.dam.api.Asset);
    escapedPath = org.apache.jackrabbit.util.Text.escapePath(resource.path);
    if (asset) {
        rtf = new com.day.cq.commons.date.RelativeTimeFormat('r', request.getResourceBundle(request.getLocale()));
        lastModified = asset.getLastModified() || properties.get("jcr:created", "");
        formattedRelativeTime = rtf.format(new Date(lastModified).getTime(), true);
    } else {
        lastModified = "";
        formattedRelativeTime = "";
    }

    return {
        lastModified: lastModified,
        formattedRelativeTime: formattedRelativeTime,
        navigationHref: request.getContextPath() + CONTENT_FRAGMENT_EDITOR_VANITY + escapedPath,
        propertiesHref: request.getContextPath() + CONTENT_FRAGMENT_PROPERTIES_PATH + escapedPath,
        // default thumbnail path for content fragments
        thumbnailUrl: "/libs/dam/cfm/admin/content/static/thumbnail_fragment.png",
        title : properties.get("jcr:content/jcr:title", resource.name),
        description: properties.get("jcr:content/jcr:description", "")
    };
});
