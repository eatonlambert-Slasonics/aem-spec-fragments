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
