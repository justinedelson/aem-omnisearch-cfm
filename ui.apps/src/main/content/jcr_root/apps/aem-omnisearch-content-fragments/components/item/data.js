var CONTENT_FRAGMENT_EDITOR_VANITY = "/editor.html";

/*global
	org
*/
use(function() {
    return {
        navigationHref: request.getContextPath() + CONTENT_FRAGMENT_EDITOR_VANITY + org.apache.jackrabbit.util.Text.escapePath(resource.path),
        // default thumbnail path for content fragments
        thumbnailUrl: "/libs/dam/cfm/admin/content/static/thumbnail_fragment.png",
        title : properties.get("jcr:content/jcr:title", resource.name),
        description: properties.get("jcr:content/jcr:description", "")
    };
});