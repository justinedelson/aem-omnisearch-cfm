# AEM OmniSearch Content Fragments

This a content package project which demostrates a custom OmniSearch module -- in this case one for Content Fragments.

## Usage

### Updating DAM Index

In order for suggestions to be properly generated, it is necessary to add two properties to the `damAssetLucene` index.

Under `/oak:index/damAssetLucene/indexRules/dam:Asset/properties`, create these two nodes:


    {
        "jcrTitle" : {
            "jcr:primaryType":"nt:unstructured",
            "nodeScopeIndex":true,
            "useInSuggest":true,
            "propertyIndex":true,
            "useInSpellcheck":true,
            "name":"jcr:content/jcr:title",
            "boost":2
        },
        "jcrDescription" : {
            "jcr:primaryType":"nt:unstructured",
            "nodeScopeIndex":true,
            "useInSuggest":true,
            "propertyIndex":true,
            "useInSpellcheck":true,
            "name":"jcr:content/jcr:description"
        }
    }

After adding these, you will also need to reindex the `damAssetLucene` index.

## Building

This project uses Maven for building. Common commands:

From the root directory, run ``mvn -PautoInstallPackage clean install`` to build the bundle and content package and install to a CQ instance.

From the bundle directory, run ``mvn -PautoInstallBundle clean install`` to build *just* the bundle and install to a CQ instance.

## Using with AEM Developer Tools for Eclipse

To use this project with the AEM Developer Tools for Eclipse, import the generated Maven projects via the Import:Maven:Existing Maven Projects wizard. Then enable the Content Package facet on the _content_ project by right-clicking on the project, then select Configure, then Convert to Content Package... In the resulting dialog, select _src/main/content_ as the Content Sync Root.

## Using with VLT

To use vlt with this project, first build and install the package to your local CQ instance as described above. Then cd to `content/src/main/content/jcr_root` and run

    vlt --credentials admin:admin checkout -f ../META-INF/vault/filter.xml --force http://localhost:4502/crx

Once the working copy is created, you can use the normal ``vlt up`` and ``vlt ci`` commands.

## Specifying CRX Host/Port

The CRX host and port can be specified on the command line with:
mvn -Dcrx.host=otherhost -Dcrx.port=5502 <goals>


