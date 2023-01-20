package com.app.check;

import com.app.Constant;
import io.quarkiverse.githubapp.event.PullRequest;
import org.kohsuke.github.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ProductJsonChecks {
    private static final Logger log = LoggerFactory.getLogger(ProductJsonChecks.class);
    private static final String PRODUCT_JSON_FILE = "product.json";

    private static final String CHECK_NAME = "product-json-check";

    private static final String CHECK_SUCCESS = "product json check succeed";
    private static final String CHECK_FAILED = "product json check failed";


    void onPullRequest(@PullRequest.Opened @PullRequest.Reopened @PullRequest.Synchronize GHEventPayload.PullRequest prPayload) throws IOException {
        log.info("Received PullRequest.{} event for PR {}", prPayload.getAction(), prPayload.getPullRequest().getHtmlUrl());
        GHRepository repository = prPayload.getRepository();
        findProductJson(prPayload.getPullRequest(), repository);
    }

    private void findProductJson(GHPullRequest pr, GHRepository repo) throws IOException {
        String sha1 = pr.getHead().getSha();
        GHCheckRun ghCheckRun = repo.createCheckRun(CHECK_NAME, sha1).withStatus(GHCheckRun.Status.IN_PROGRESS).create();
        CheckResult checkResult = new CheckResult();
        String ref = pr.getHead().getRef();
        List<GHContent> directoryContent = repo.getDirectoryContent(".", ref);
        for (GHContent ghContent : directoryContent) {
            recursion(ghContent, checkResult);
        }
        log.debug("Updating status of commit {} in repo {} to {}", sha1, repo.getHtmlUrl(), checkResult.isPass());
        long checkRunId = ghCheckRun.getId();
        if (checkResult.isPass()) {
            checkResult.setTitle(CHECK_SUCCESS);
            checkResult.resetMessage().append(Constant.SUCCESS);
            updateCheckRun(checkRunId, repo, GHCheckRun.Conclusion.SUCCESS, checkResult);
        } else {
            checkResult.setTitle(CHECK_FAILED);
            updateCheckRun(checkRunId, repo, GHCheckRun.Conclusion.FAILURE, checkResult);
        }
    }

    private void recursion(GHContent ghContent, CheckResult checkResult) throws IOException {
        if (ghContent.isDirectory()) {
            for (GHContent content : ghContent.listDirectoryContent()) {
                recursion(content, checkResult);
            }
            return;
        }
        log.info(ghContent.getName());
        //validate product json file
        if (ghContent.getName().endsWith(PRODUCT_JSON_FILE)) {
            InputStream productJsonInputStream = ghContent.read();
            String s = JsonSchemaValidator.validateProductJson(productJsonInputStream);
            if (!Constant.SUCCESS.equals(s)) {
                checkResult.setPass(false);
                checkResult.append(ghContent.getName())
                        .append(" validate failed!")
                        .append("\n")
                        .append(s)
                        .append("\n");
            }
        }
    }

    private void updateCheckRun(long checkRunId, GHRepository repo, GHCheckRun.Conclusion conclusion, CheckResult checkResult) throws IOException {
        GHCheckRunBuilder checkRunBuilder = repo.updateCheckRun(checkRunId);
        checkRunBuilder.withStatus(GHCheckRun.Status.COMPLETED);
        checkRunBuilder.withConclusion(conclusion);
        GHCheckRunBuilder.Output output = new GHCheckRunBuilder.Output(checkResult.getTitle(), checkResult.getMessage());
        checkRunBuilder.add(output);
        checkRunBuilder.create();
    }
}
