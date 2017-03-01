package org.obsidiantoaster.quickstart;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.arquillian.cube.kubernetes.api.Session;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.jayway.awaitility.Awaitility.await;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
public class OpenshiftIT {

    @ArquillianResource
    KubernetesClient client;

    @ArquillianResource
    Session session;

    @Before
    public void deployDatabaseIfNeeded() throws IOException {
        List<Pod> pods = client.pods().inNamespace(session.getNamespace())
            .withLabel("app", "my-database").list().getItems();
        if (pods.isEmpty()) {
            File list = new File("src/test/resources/templates/database.yml");
            try (FileInputStream fis = new FileInputStream(list)){
                List<HasMetadata> entities = client.adapt(OpenShiftClient.class).load(fis).createOrReplace();
                System.out.println("Database deployed, " + entities.size() + " object(s) created.");
            }
        }

        // Wait for readiness
        await().atMost(1, TimeUnit.MINUTES).until(() ->
            client.pods().withLabel("app", "my-database").list().getItems()
                .stream()
                .filter(pod -> "running".equalsIgnoreCase(pod.getStatus().getPhase()))
                .collect(Collectors.toList()).size() == 1
        );

        System.out.println("Database ready");
    }

    @Before
    public void deploySecretIfNeeded() throws IOException {
        Secret secret = client.secrets().inNamespace(session.getNamespace())
            .withName("my-database-secret").get();

        if (secret == null) {
            File desc = new File("credentials-secret.yml");
            try (FileInputStream fis = new FileInputStream(desc)){
                List<HasMetadata> entities = client.adapt(OpenShiftClient.class).load(fis).createOrReplace();
                System.out.println("Secret deployed, " + entities.size() + " object(s) created.");
            }
        }
    }

    @Test
    public void testWeReachReadiness() throws Exception {
        List<Pod> pods = client.pods().inNamespace(session.getNamespace()).list().getItems();
        await().atMost(1, TimeUnit.MINUTES).until(() ->
            pods.stream().filter(pod ->
                "running".equalsIgnoreCase(pod.getStatus().getPhase()))
                .collect(Collectors.toList()).size() >= 1
        );
    }
}
