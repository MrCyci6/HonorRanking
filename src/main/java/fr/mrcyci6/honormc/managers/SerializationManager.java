package fr.mrcyci6.honormc.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.mrcyci6.honormc.managers.ClassementManager;

public class SerializationManager {

    private Gson gson;

    public SerializationManager() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .disableHtmlEscaping()
                .create();
    }

    public String serialize(ClassementManager classementManager) {
        return this.gson.toJson(classementManager);
    }

    public ClassementManager deserialize(String json) {
        return this.gson.fromJson(json, ClassementManager.class);
    }
}
