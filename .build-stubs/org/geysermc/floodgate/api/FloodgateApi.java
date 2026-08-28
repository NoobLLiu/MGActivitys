package org.geysermc.floodgate.api;

import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.UUID;

/** Compile-time stub of FloodgateApi (only the surface used by MGActivitys). */
public final class FloodgateApi {

    private FloodgateApi() {
    }

    public static FloodgateApi getInstance() {
        return null;
    }

    public void sendForm(UUID uuid, SimpleForm.Builder form) {
    }

    public void sendForm(UUID uuid, CustomForm.Builder form) {
    }
}