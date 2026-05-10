package Prototype.SpecificationParser;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("add")
public class AddTransformation extends TransformationFormat {
    Integer index;
    String key;
    String value;


    public AddTransformation() {
        setType("add");
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getIndex() {
        return index;
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
