package org.geysermc.cumulus.form;

import org.geysermc.cumulus.response.CustomFormResponse;

import java.util.function.Consumer;

/** Compile-time stub of Cumulus CustomForm. */
public final class CustomForm {

    private CustomForm() {
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

        public Builder label(String text) {
            return this;
        }

        public Builder input(String label, String placeholder, String defaultText) {
            return this;
        }

        public Builder toggle(String label, boolean defaultValue) {
            return this;
        }

        public void validResultHandler(Consumer<CustomFormResponse> handler) {
        }

        public void closedResultHandler(Runnable handler) {
        }
    }
}