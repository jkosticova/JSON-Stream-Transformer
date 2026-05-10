package Prototype.SpecificationParser;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("replace")
public class ReplaceTransformation extends TransformationFormat {
    String key;
    String value;

    public ReplaceTransformation() {
        setType("replace");
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Transformation.Transformation " + getType()
                + " on " + getPath();
    }
}
