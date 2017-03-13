package org.obsidiantoaster.quickstart;

import com.jayway.restassured.response.Response;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.restassured.RestAssured.get;

/**
 * Check the behavior of the application when running in OpenShift.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OpenShiftIT {

  private static OpenShiftTestAssistant assistant = new OpenShiftTestAssistant();

  @BeforeClass
  public static void prepare() throws Exception {
    assistant.deployApplication();

    // Deploy the database and wait until it's ready.
    assistant.deploy("database", new File("src/test/resources/templates/database.yml"));
    assistant.awaitPodReadinessOrFail(
        pod -> "my-database".equals(pod.getMetadata().getLabels().get("app"))
    );
    System.out.println("Database ready");

    // Deploy the secret
    assistant.deploy("secret", new File("credentials-secret.yml"));

    assistant.deployApplication();
  }

  @AfterClass
  public static void cleanup() {
    assistant.cleanup();
  }


  @Test
  public void testA_WeReachReadiness() throws Exception {
    assistant.awaitApplicationReadinessOrFail();

    await().atMost(5, TimeUnit.MINUTES).until(() -> {
      try {
        Response response = get();
        return response.getStatusCode() < 500;
      } catch (Exception e) {
        return false;
      }
    });
  }
}
