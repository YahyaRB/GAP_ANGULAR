package ma.gap.controller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ma.gap.entity.BiotimeUser;
import ma.gap.entity.ResponseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class RestTokenController {

    @Autowired
    RestTemplate restTemplate;


    private BiotimeUser getAuthenticationUser() {
        BiotimeUser user = new BiotimeUser();
        user.setUsername("e.abderrahmane");
        user.setPassword("User@2021");
        return user;
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    private String getBody(final BiotimeUser user) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(user);
    }


    public ResponseEntity<ResponseToken> getResponse() throws JsonProcessingException{

        String AUTHENTICATION_URL = "http://192.168.77.19:8060/jwt-api-token-auth/";


        // create user authentication object
        BiotimeUser authenticationUser = getAuthenticationUser();

        // convert the user authentication object to JSON
        String authenticationBody = getBody(authenticationUser);

        // create headers specifying that it is JSON request
        HttpHeaders authenticationHeaders = getHeaders();
        HttpEntity<String> authenticationEntity = new HttpEntity<String>(authenticationBody,
                authenticationHeaders);

        // Authenticate User and get JWT
        ResponseEntity<ResponseToken> authenticationResponse = restTemplate.exchange(AUTHENTICATION_URL,
                HttpMethod.POST, authenticationEntity, ResponseToken.class);

        return authenticationResponse;
    }

    @RequestMapping(value = "/getProrataEmploye", method = RequestMethod.GET)
    public String getProrataEmploye(int page) throws JsonProcessingException {
        String response = null;
        String PRORATA_EMP = "http://192.168.77.19:8060/iclock/api/transactions/?start_time=2021-09-21 00:00:00&end_time=2021-10-20 00:00:00&page="+page;

        ResponseEntity<ResponseToken> authenticationResponse = getResponse();

        if (authenticationResponse.getStatusCode().equals(HttpStatus.OK)) {
            String token = "JWT " + authenticationResponse.getBody().getToken();
            HttpHeaders headers = getHeaders();
            headers.set("Authorization", token);
            HttpEntity<String> jwtEntity = new HttpEntity<String>(headers);
            // Use Token to get Response
            ResponseEntity<String> helloResponse = restTemplate.exchange(PRORATA_EMP, HttpMethod.GET, jwtEntity,
                    String.class);
            if (helloResponse.getStatusCode().equals(HttpStatus.OK)) {
                response = helloResponse.getBody();
            }
        }

        return response;

    }

}
