package Prototype.SpecificationParser;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("remove")
public class RemoveTransformation extends TransformationFormat {
    public RemoveTransformation() {
        setType("remove");
    }

    @Override
    public String toString() {
        return "Transformation.Transformation " + getType()
                + " on " + getPath();
    }
}
