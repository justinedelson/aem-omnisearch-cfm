use(function() {
	log.info(resource);
    return {
        title : properties.get("jcr:content/jcr:title", resource.name),
        description: properties.get("jcr:content/jcr:description", "")
    };
});