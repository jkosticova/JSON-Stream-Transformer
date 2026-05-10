package Prototype.SpecificationParser;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("copy")
public class CopyTransformation extends TransformationFormat {
    Integer index;
    String key;
    String value;
    String destPath;

    public CopyTransformation() {
        setType("copy");
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

    public String getDestPath() {
        return destPath;
    }

    public void setDestPath(String destPath) {
        this.destPath = destPath;
    }

    @Override
    public String toString() {
        return "Transformation.Transformation " + getType()
                + " on " + getPath();
    }
}
