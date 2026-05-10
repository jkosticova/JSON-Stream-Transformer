package Prototype.SpecificationParser;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("identity")
public class IdentityTransformation extends TransformationFormat {

    public IdentityTransformation() {
        setType("identity");
        setPath("$");
    }

    @Override
    public String toString() {
        return "Transformation.Transformation " + getType()
                + " on " + getPath();
    }
}
