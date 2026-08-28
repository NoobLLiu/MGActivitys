package org.geysermc.cumulus.form;

import org.geysermc.cumulus.response.SimpleFormResponse;

import java.util.function.Consumer;

/** Compile-time stub of Cumulus SimpleForm. */
public final class SimpleForm {

    private SimpleForm() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        Builder() {
        }

        public Builder title(String title) {
            return this;
        }

        public Builder content(String content) {
            return this;
        }

        public Builder button(String text) {
            return this;
        }

        public void validResultHandler(Consumer<SimpleFormResponse> handler) {
        }
    }
}