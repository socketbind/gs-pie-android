
package org.glasshack.pie.model;

import java.util.ArrayList;
import java.util.List;


public class Recipe {

    private Integer __v;

    private String _id;

    private String description;

    private String indigrients;

    private String name;

    private String stepsText;

    private List<Step> steps = new ArrayList<Step>();

    private Image image;

    private Integer rating;

    public Integer get__v() {
        return __v;
    }

    public void set__v(Integer __v) {
        this.__v = __v;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIndigrients() {
        return indigrients;
    }

    public void setIndigrients(String indigrients) {
        this.indigrients = indigrients;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStepsText() {
        return stepsText;
    }

    public void setStepsText(String stepsText) {
        this.stepsText = stepsText;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }


}
