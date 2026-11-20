# AEM Spec Fragments

Electronic component specs stored as AEM Content Fragments, with manufacturer and
category fragments resolved into a single JSON feed.

This repo holds the **AEM 6.4** version. 6.4 has no Fragment Reference type and no
GraphQL, so references are stored as Content Reference paths and resolved in code:

- `ComponentSpec` (Sling Model): reads a Component fragment, checks its model, and
  follows the manufacturer and category paths to their fragments.
- `ComponentSpecServlet`: `GET /content/dam/portfolio/components.specs.json` returns
  every component fragment in the folder with references resolved.

The Cloud Service version replaces both with a Fragment Reference field and a GraphQL
persisted query, which makes the pair a before-and-after migration example.

Case study: [main--eds--eatonlambert-slasonics.aem.live/case-studies/spec-fragments](https://main--eds--eatonlambert-slasonics.aem.live/case-studies/spec-fragments)

## Sample response

`GET /content/dam/portfolio/components.specs.json`, with the sample fragments in `ui.content`:

```json
[
  {
    "valid": true,
    "partNumber": "LM358",
    "name": "Dual Op Amp",
    "packageType": "DIP",
    "pinCount": 8,
    "manufacturer": "Texas Instruments",
    "category": "Op Amps",
    "path": "/content/dam/portfolio/components/lm358"
  },
  {
    "valid": true,
    "partNumber": "TL072",
    "name": "Low-Noise JFET Op Amp",
    "packageType": "DIP",
    "pinCount": 8,
    "manufacturer": "Texas Instruments",
    "category": "Op Amps",
    "path": "/content/dam/portfolio/components/tl072"
  },
  {
    "valid": true,
    "partNumber": "LM7805",
    "name": "5V Linear Regulator",
    "packageType": "TO-220",
    "pinCount": 3,
    "manufacturer": "Texas Instruments",
    "category": "Voltage Regulators",
    "path": "/content/dam/portfolio/components/lm7805"
  }
]
```

## Modules

- `core`: OSGi bundle with the Sling Model and servlet
- `ui.content`: content package with the `/conf/portfolio` configuration, the three
  Content Fragment Models (Component, Manufacturer, Category), the DAM folders and
  sample fragments

## Build and install

Requires Java 8 and Maven 3.9.

```
mvn clean install
mvn clean install -PautoInstallBundle,autoInstallPackage "-Dsling.password=<admin password>"
```

## Next

- Paging or a query for the full 1,000-part set
- Dispatcher rules allowing the `specs` selector on publish
- Cloud Service version with Fragment References and GraphQL persisted queries
