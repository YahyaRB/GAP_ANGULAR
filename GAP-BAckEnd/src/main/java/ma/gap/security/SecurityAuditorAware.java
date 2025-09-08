package ma.gap.security;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ma.gap.config.GlobalVariableConfig;

import java.util.Optional;
@AllArgsConstructor
public class SecurityAuditorAware implements AuditorAware<String> {
private GlobalVariableConfig globalVariableConfig;

    public Optional<String> getCurrentAuditor() {
        return Optional.of(globalVariableConfig.userConnected.getUsername());
    }

}
