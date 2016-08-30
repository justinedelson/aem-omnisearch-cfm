use(function() {
	log.info(resource);
    return {
        // default thumbnail path for content fragments
        thumbnailUrl: "/libs/dam/cfm/admin/content/static/thumbnail_fragment.png",
        title : properties.get("jcr:content/jcr:title", resource.name),
        description: properties.get("jcr:content/jcr:description", "")
    };
});