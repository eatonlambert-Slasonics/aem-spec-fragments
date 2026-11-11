package com.tone.portfolio.core.models;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import javax.annotation.PostConstruct;

/**
 * Reads one Component content fragment and resolves its manufacturer and
 * category Content References (AEM 6.4 has no Fragment Reference type).
 */
@Model(adaptables = Resource.class,
       defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ComponentSpec {

    static final String MODELS = "/conf/portfolio/settings/dam/cfm/models/";
    static final String COMPONENT_MODEL = MODELS + "component";
    static final String MANUFACTURER_MODEL = MODELS + "manufacturer";
    static final String CATEGORY_MODEL = MODELS + "category";

    @Self
    private Resource resource;

    private boolean valid;
    private String partNumber;
    private String name;
    private String packageType;
    private Integer pinCount;
    private String manufacturer;
    private String category;

    @PostConstruct
    protected void init() {
        ContentFragment cf = resource.adaptTo(ContentFragment.class);
        if (cf == null || !COMPONENT_MODEL.equals(modelOf(resource))) {
            return; // not a component fragment; valid stays false
        }
        valid = true;
        partNumber = text(cf, "partNumber");
        name = text(cf, "name");
        packageType = text(cf, "packageType");
        pinCount = number(text(cf, "pinCount"));
        manufacturer = referencedName(text(cf, "manufacturer"), MANUFACTURER_MODEL);
        category = referencedName(text(cf, "category"), CATEGORY_MODEL);
    }

    /** Follows a Content Reference path to another fragment and returns its "name". */
    private String referencedName(String path, String expectedModel) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        ResourceResolver resolver = resource.getResourceResolver();
        Resource target = resolver.getResource(path);
        if (target == null || !expectedModel.equals(modelOf(target))) {
            return null; // broken link or wrong kind of fragment
        }
        ContentFragment ref = target.adaptTo(ContentFragment.class);
        return ref == null ? null : text(ref, "name");
    }

    /** Model path stored on the fragment (verify location in CRXDE). */
    static String modelOf(Resource fragment) {
        Resource data = fragment.getChild("jcr:content/data");
        if (data == null) {
            return null;
        }
        return data.getValueMap().get("cq:model", String.class);
    }

    private static String text(ContentFragment cf, String element) {
        ContentElement el = cf.getElement(element);
        String value = el == null ? null : el.getContent();
        return StringUtils.trimToNull(value);
    }

    private static Integer number(String value) {
        try {
            return value == null ? null : Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean isValid() { return valid; }
    public String getPath() { return resource.getPath(); }
    public String getPartNumber() { return partNumber; }
    public String getName() { return name; }
    public String getPackageType() { return packageType; }
    public Integer getPinCount() { return pinCount; }
    public String getManufacturer() { return manufacturer; }
    public String getCategory() { return category; }
}
