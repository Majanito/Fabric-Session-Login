package dev.majanito.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import dev.majanito.SessionIDLoginMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.Session;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    @Final
    private YggdrasilAuthenticationService authenticationService;

    @Shadow
    @Final
    public File runDirectory;

    @Unique
    private UUID lastProfileKeysUuid = null;

    @Unique
    private String lastProfileKeysToken = null;

    @Unique
    private ProfileKeys cachedProfileKeys = null;

    @Inject(method = "getSession", at = @At("HEAD"), cancellable = true)
    private void onGetSession(CallbackInfoReturnable<Session> cir) {
        if (!SessionIDLoginMod.overrideSession) {
            return;
        }

        cir.setReturnValue(SessionIDLoginMod.currentSession);
    }

    @Inject(method = "getProfileKeys", at = @At("HEAD"), cancellable = true)
    private void onGetProfileKeys(CallbackInfoReturnable<ProfileKeys> cir) {
        if (!SessionIDLoginMod.overrideSession) {
            return;
        }

        Session currentSession = SessionIDLoginMod.currentSession;
        UUID currentUuid = currentSession.getUuidOrNull();
        String currentToken = currentSession.getAccessToken();

        // Check if session changed (UUID or token)
        if (lastProfileKeysUuid == null ||
                !lastProfileKeysUuid.equals(currentUuid) ||
                lastProfileKeysToken == null ||
                !lastProfileKeysToken.equals(currentToken)) {

            lastProfileKeysUuid = currentUuid;
            lastProfileKeysToken = currentToken;

            SessionIDLoginMod.LOGGER.info("Session changed, creating new ProfileKeys for: {}", currentSession.getUsername());

            try {
                // Create a new UserApiService with the new session's token
                UserApiService userApiService = authenticationService.createUserApiService(currentToken);

                // Get the profile keys path
                Path profileKeysPath = runDirectory.toPath().resolve("profilekeys");

                // Create new ProfileKeys with the new UserApiService and current session
                cachedProfileKeys = ProfileKeys.create(userApiService, currentSession, profileKeysPath);

                SessionIDLoginMod.LOGGER.info("Successfully created new ProfileKeys for: {}", currentSession.getUsername());
            } catch (Exception e) {
                SessionIDLoginMod.LOGGER.error("Failed to create ProfileKeys: {}", e.getMessage());
                cachedProfileKeys = null;
            }
        }

        if (cachedProfileKeys != null) {
            cir.setReturnValue(cachedProfileKeys);
        }
    }
}
