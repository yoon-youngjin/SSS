package dev.aquashdw.auth.client;

import dev.aquashdw.auth.client.entity.OAuthClientEntity;
import dev.aquashdw.auth.client.entity.RedirectEntity;
import dev.aquashdw.auth.client.repo.ClientRepository;
import dev.aquashdw.auth.client.repo.RedirectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Primary
@Service
// 네이버 아이디로 로그인에서 app 생성 할 때 app 이름, 받아올 정보를 등록하는 인터페이스
public class RegisteredClientService implements RegisteredClientRepository {
    private static final Logger logger = LoggerFactory.getLogger(RegisteredClientService.class);
    private final ClientRepository clientRepository;
    private final RedirectRepository redirectRepository;

    public RegisteredClientService(
            @Autowired
            ClientRepository clientRepository,
            @Autowired
            RedirectRepository redirectRepository
    ) {
        this.clientRepository = clientRepository;
        this.redirectRepository = redirectRepository;


        OAuthClientEntity newClient = new OAuthClientEntity();
        newClient.setUid(UUID.randomUUID().toString());
        newClient.setClientId("likelion-client");
        // {noop}: 비밀번호를 저장할 때 사용하는 알고리즘 중 하나, no operation -> 암호화x
        newClient.setClientSecret("{noop}secret");
        newClient = this.clientRepository.save(newClient);

        RedirectEntity redirectEntity1 = new RedirectEntity(
                newClient,
                "http://127.0.0.1:9080/login/oauth2/code/likelion-oidc"
        );
        this.redirectRepository.save(redirectEntity1);

        RedirectEntity redirectEntity2 = new RedirectEntity(
                newClient,
                "http://127.0.0.1:9080/authorized"
        );
        this.redirectRepository.save(redirectEntity2);
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        OAuthClientEntity newClient = new OAuthClientEntity();
        newClient.setUid(registeredClient.getId());
        newClient.setClientId(registeredClient.getClientId());
        List<RedirectEntity> redirectEntities = new ArrayList<>();
        for (String redirectUri: registeredClient.getRedirectUris()){
            RedirectEntity redirectEntity = new RedirectEntity();
            redirectEntity.setRedirectUri(redirectUri);
            redirectEntities.add(redirectRepository.save(redirectEntity));
        }

        newClient.setRedirectUris(redirectEntities);
        clientRepository.save(newClient);
    }

    @Override
    public RegisteredClient findById(String id) {
        return this.entityToClientInterface(clientRepository.findFirstByUid(id));
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return this.entityToClientInterface(clientRepository.findFirstByClientId(clientId));
    }

    private RegisteredClient entityToClientInterface(OAuthClientEntity clientEntity){
        RegisteredClient.Builder clientBuilder = RegisteredClient.withId(clientEntity.getUid())
                .clientId(clientEntity.getClientId())
                .clientSecret(clientEntity.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .scope(OidcScopes.OPENID)
                .scope("read");

        for (RedirectEntity uriEntity: clientEntity.getRedirectUris()){
            clientBuilder.redirectUri(uriEntity.getRedirectUri());
        }

        return clientBuilder.build();
    }
}
