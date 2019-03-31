package cz.hudecekpetr.snowride.semantics.codecompletion;

import cz.hudecekpetr.snowride.fx.IAutocompleteOption;
import javafx.scene.image.Image;

public class ExternalKeyword implements IAutocompleteOption {

    private final String canonicalName;
    private final ExternalLibrary library;

    public ExternalKeyword(String canonicalName, ExternalLibrary library) {

        this.canonicalName = canonicalName;
        this.library = library;
    }

    @Override
    public Image getAutocompleteIcon() {
        return library.getIcon();
    }

    @Override
    public String toString() {
        return this.canonicalName;
    }
}