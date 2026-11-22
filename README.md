# AEM Spec Fragments

Electronic component specs modeled as AEM Content Fragments and served as one JSON feed, with manufacturer and category references resolved on the server. Built for AEM 6.4, with an AEM as a Cloud Service GraphQL version planned.

## Why this exists

I led an AEM workstream that moved about 1,000 electronic component technical specs from a legacy relational database into Content Fragments. This repo rebuilds the delivery side of that pattern on its own: structured part data in fragments, related data (manufacturer, category) kept in its own fragments, and one endpoint that returns every spec with those relationships resolved.

On AEM 6.4 that last part takes code. 6.4 has no Fragment Reference field and no GraphQL, so references are stored as Content Reference paths and followed in Java. The planned Cloud Service version replaces that code with a Fragment Reference field and a GraphQL persisted query, so the two versions together read as a before-and-after migration example.

Case study: [main--eds--eatonlambert-slasonics.aem.live/case-studies/spec-fragments](https://main--eds--eatonlambert-slasonics.aem.live/case-studies/spec-fragments)

## What's in the repo today

| Piece | Status |
| --- | --- |
| `ComponentSpec` Sling Model | Done |
| `ComponentSpecServlet` JSON feed | Done |
| Maven build: OSGi bundle, content package, auto-install profiles | Done |
| Content package filter for `/conf/portfolio` and `/content/dam/portfolio` | Done |
| Content Fragment Models, `/conf` configuration, DAM folders, sample fragments (3 parts, 2 categories, 1 manufacturer) | Done |
| Unit tests | Not started |
| AEM as a Cloud Service GraphQL version | Planned |

## How it works

```
GET /content/dam/portfolio/components.specs.json
        |
        v
ComponentSpecServlet   (sling:Folder / sling:OrderedFolder, selector "specs", extension "json", GET)
        |  for each direct child of the folder
        v
ComponentSpec          (Sling Model adapted from the child Resource)
        |  1. adapt to ContentFragment
        |  2. check jcr:content/data/@cq:model is the Component model
        |  3. read partNumber, name, packageType, pinCount
        |  4. follow manufacturer and category paths, check their model, read "name"
        v
List<ComponentSpec>  ->  Jackson (null fields omitted)  ->  JSON array
```

### Content Fragment Models

Three models live under `/conf/portfolio/settings/dam/cfm/models/` and install with the content package:

| Model | Elements | Read by the code |
| --- | --- | --- |
| `component` | `partNumber` (text, required), `name` (text), `packageType` (enumeration: DIP, SOIC, TO-92, TO-220, QFN, SMD), `pinCount` (number), `manufacturer` and `category` (Content References rooted at the manufacturers and categories folders) | All six |
| `manufacturer` | `name` (text) | `name` |
| `category` | `name`, `slug` (text) | `name` only |

### ComponentSpec (Sling Model)

- Adapts a DAM resource to `ContentFragment` and marks itself valid only when the fragment's model is the Component model, so other assets or fragment types in the folder are skipped.
- Reads each element as trimmed text. `pinCount` is parsed to an integer and left out when it isn't a number.
- Follows the manufacturer and category paths with the request's resource resolver, checks that the target fragment uses the expected model, and returns its `name`. A broken path or a fragment of the wrong type leaves that field empty instead of raising an error.

### ComponentSpecServlet

- A `SlingSafeMethodsServlet` (GET only) registered on `sling:Folder` and `sling:OrderedFolder` with the `specs` selector and `json` extension.
- Lists the folder's direct children, adapts each one to `ComponentSpec`, keeps the valid ones, and writes them as a JSON array with Jackson, leaving out null fields.
- Because it binds to folder resource types, the `specs` selector works on any DAM folder, not only `/content/dam/portfolio`.

## Sample output

Expected response for the sample content in `ui.content`, worked out from the fragments' data and the Sling Model's getters rather than captured from a running instance.

```json
[
  {
    "valid": true,
    "path": "/content/dam/portfolio/components/lm358",
    "partNumber": "LM358",
    "name": "Dual Op Amp",
    "packageType": "DIP",
    "pinCount": 8,
    "manufacturer": "Texas Instruments",
    "category": "Op Amps"
  },
  {
    "valid": true,
    "path": "/content/dam/portfolio/components/lm7805",
    "partNumber": "LM7805",
    "name": "5V Linear Regulator",
    "packageType": "TO-220",
    "pinCount": 3,
    "manufacturer": "Texas Instruments",
    "category": "Voltage Regulators"
  },
  {
    "valid": true,
    "path": "/content/dam/portfolio/components/tl072",
    "partNumber": "TL072",
    "name": "Low-Noise JFET Op Amp",
    "packageType": "DIP",
    "pinCount": 8,
    "manufacturer": "Texas Instruments",
    "category": "Op Amps"
  }
]
```

## Build and deploy

Requires Java 8, Maven 3.9 and a local AEM 6.4 author. The defaults are `localhost:4502` and user `admin`, set in the root `pom.xml` as `aem.host`, `aem.port` and `sling.user`.

```
# Build the bundle and the content package
mvn clean install

# Build and install both into the local author
mvn clean install -PautoInstallBundle,autoInstallPackage "-Dsling.password=<admin password>"
```

`autoInstallBundle` installs the bundle through the Felix web console, and `autoInstallPackage` uploads the content package through Package Manager. Then request the feed:

```
curl -u admin:<admin password> http://localhost:4502/content/dam/portfolio/components.specs.json
```

## Project layout

```
pom.xml                  parent POM (AEM 6.4 uber-jar, Java 8)
core/                    OSGi bundle
  src/main/java/com/tone/portfolio/core/
    models/ComponentSpec.java
    servlets/ComponentSpecServlet.java
ui.content/              content package
  src/main/content/
    META-INF/vault/filter.xml
    jcr_root/conf/portfolio/settings/dam/cfm/models/
      component/  manufacturer/  category/
    jcr_root/content/dam/portfolio/
      components/     lm358, lm7805, tl072
      manufacturers/  texas-instruments
      categories/     op-amps, voltage-regulators
```

## Design choices

- **Validate by model:** each fragment is checked against its Content Fragment Model before any field is read, so one folder can hold mixed content without polluting the feed.
- **Fail soft on references:** a deleted manufacturer or a mis-pointed path drops that one field instead of breaking the whole feed.
- **Merge-mode filter:** `/content/dam/portfolio` installs in merge mode, so reinstalling the package never wipes authored fragments.
- **Read-only endpoint:** the servlet extends `SlingSafeMethodsServlet` and answers GET only.

## Roadmap

- Unit tests for `ComponentSpec`, for example with AEM Mocks
- Paging or a query for the full 1,000-part set, since the servlet reads one folder's direct children
- Dispatcher rules allowing the `specs` selector on publish
- AEM as a Cloud Service version: Fragment Reference fields in place of Content Reference paths, and a GraphQL persisted query in place of the servlet

## AEM 6.4 today vs Cloud Service (planned)

|  | AEM 6.4 (this repo) | AEM as a Cloud Service (planned) |
| --- | --- | --- |
| Relationships | Content Reference path, resolved in Java | Fragment Reference field |
| Delivery | Custom Sling servlet, `.specs.json` | GraphQL persisted query |
