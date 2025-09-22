package ma.gap.security;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ma.gap.config.GlobalVariableConfig;
import org.springframework.stereotype.Component;

import java.util.Optional;
@Component
public class SecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() ||
                    authentication instanceof AnonymousAuthenticationToken) {
                // Retourner un utilisateur par défaut au lieu de null
                return Optional.of("system");
            }

            String username = authentication.getName();
            if (username == null || username.trim().isEmpty()) {
                return Optional.of("system");
            }

            return Optional.of(username);

        } catch (Exception e) {
            // Log l'erreur et retourner un utilisateur par défaut
            System.err.println("Erreur lors de la récupération de l'auditeur: " + e.getMessage());
            return Optional.of("system");
        }
    }
}