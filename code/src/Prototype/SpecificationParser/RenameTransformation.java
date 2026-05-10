package Prototype.SpecificationParser;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("rename")
public class RenameTransformation extends TransformationFormat {
    String key;

    public RenameTransformation() {
        setType("rename");
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "Transformation.Transformation " + getType()
                + " on " + getPath()
                + " using " + getKey();
    }
}
