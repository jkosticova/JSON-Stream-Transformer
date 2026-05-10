package Prototype.SpecificationParser;

import com.fasterxml.jackson.annotation.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(RenameTransformation.class),
        @JsonSubTypes.Type(IdentityTransformation.class),
        @JsonSubTypes.Type(RemoveTransformation.class),
        @JsonSubTypes.Type(AddTransformation.class),
        @JsonSubTypes.Type(ReplaceTransformation.class),
        @JsonSubTypes.Type(CopyTransformation.class),
        @JsonSubTypes.Type(MoveTransformation.class)})
public abstract class TransformationFormat {
    String path;
    private String type;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
