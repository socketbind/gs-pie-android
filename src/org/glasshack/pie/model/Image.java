
package org.glasshack.pie.model;

public class Image {


    private String _id;

    private String secure_url;

    private String url;

    private String resource_type;

    private String format;

    private Integer height;

    private Integer width;

    private String signature;

    private Integer version;

    private String public_id;

    private String glassUrl;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getSecure_url() {
        return secure_url;
    }

    public void setSecure_url(String secure_url) {
        this.secure_url = secure_url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getResource_type() {
        return resource_type;
    }

    public void setResource_type(String resource_type) {
        this.resource_type = resource_type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getPublic_id() {
        return public_id;
    }

    public void setPublic_id(String public_id) {
        this.public_id = public_id;
    }

    public String getGlassUrl() {
        return glassUrl;
    }

    public void setGlassUrl(String glassUrl) {
        this.glassUrl = glassUrl;
    }
}
