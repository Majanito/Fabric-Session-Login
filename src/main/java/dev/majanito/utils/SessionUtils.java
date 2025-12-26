package dev.majanito.utils;

import dev.majanito.SessionIDLoginMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.util.Optional;
import java.util.UUID;

public class SessionUtils {
    public static String getUsername(){
        return MinecraftClient.getInstance().getSession().getUsername();
    }

    public static Session getSession(){
        return MinecraftClient.getInstance().getSession();
    }

    public static Session createSession(String username, String uuidString, String ssid) {

        if (uuidString.length() == 32) {
            uuidString = uuidString.substring(0, 8) + "-" +
                    uuidString.substring(8, 12) + "-" +
                    uuidString.substring(12, 16) + "-" +
                    uuidString.substring(16, 20) + "-" +
                    uuidString.substring(20);
        }


        return new Session(
                username,
                UUID.fromString(uuidString),
                ssid,
                Optional.empty(),
                Optional.empty(),
                Session.AccountType.MSA
        );
    }

    public static Session createSession(String username, UUID uuid, String ssid) {

        return new Session(
                username,
                uuid,
                ssid,
                Optional.empty(),
                Optional.empty(),
                Session.AccountType.MSA
        );
    }

    public static void setSession(Session session){
        SessionIDLoginMod.currentSession = session;
    }

    public static void restoreSession(){
        SessionIDLoginMod.currentSession = SessionIDLoginMod.originalSession;
    }
}
