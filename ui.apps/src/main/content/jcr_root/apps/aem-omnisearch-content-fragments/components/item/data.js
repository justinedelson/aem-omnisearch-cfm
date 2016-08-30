/*global org, com, java*/
var CONTENT_FRAGMENT_EDITOR_VANITY = "/editor.html";

use(function() {
    var resourceResolver, asset, lastModified, rtf, date, formattedRelativeTime;

    resourceResolver = resource.resourceResolver;
    asset = resource.adaptTo(com.day.cq.dam.api.Asset);
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
        navigationHref: request.getContextPath() + CONTENT_FRAGMENT_EDITOR_VANITY + org.apache.jackrabbit.util.Text.escapePath(resource.path),
        // default thumbnail path for content fragments
        thumbnailUrl: "/libs/dam/cfm/admin/content/static/thumbnail_fragment.png",
        title : properties.get("jcr:content/jcr:title", resource.name),
        description: properties.get("jcr:content/jcr:description", "")
    };
});
