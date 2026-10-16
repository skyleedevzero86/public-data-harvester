package com.antock.api.coseller.presentation;

import com.antock.api.coseller.application.service.Nplus1MeasurementService;
import com.antock.api.coseller.application.service.Nplus1Probe;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Controller
@Profile({"dev", "local"})
@RequiredArgsConstructor
public class Nplus1ReportController {

    private static final DateTimeFormatter MEASURED_AT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Nplus1MeasurementService measurementService;

    @GetMapping(value = "/dev/nplus1", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String report(@RequestParam(defaultValue = "2000") int limit) {
        int bounded = Math.min(Math.max(limit, 1), 2000);
        Nplus1Probe.Report report = measurementService.measure(bounded);
        return render(report);
    }

    private String render(Nplus1Probe.Report report) {
        String measuredAt = LocalDateTime.now().format(MEASURED_AT);
        if (report.rows() == 0) {
            return page(measuredAt, """
                    <p>corp_mast에 행이 없어 쿼리 수를 세지 못했습니다.</p>
                    """);
        }
        int saved = report.before().queries() - report.after().queries();
        return page(measuredAt, """
                <p class="lead">법인 %s건의 이력을 한 번 읽을 때 실행된 JDBC 문 개수입니다. @BatchSize(%d).</p>
                <div class="nums">
                  <div>
                    <div class="label">개선 전, 건별 조회</div>
                    <div class="num before">%s회</div>
                    <div class="meta">%d ms, 이력 %s건</div>
                  </div>
                  <div>
                    <div class="label">개선 후, BatchSize</div>
                    <div class="num after">%s회</div>
                    <div class="meta">%d ms, 이력 %s건</div>
                  </div>
                </div>
                <p class="saved">쿼리 %s회를 줄였습니다.</p>
                """.formatted(
                format(report.rows()),
                report.batchSize(),
                format(report.before().queries()),
                report.before().millis(),
                format(report.before().historyRows()),
                format(report.after().queries()),
                report.after().millis(),
                format(report.after().historyRows()),
                format(saved)));
    }

    private String page(String measuredAt, String body) {
        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                  <meta charset="utf-8">
                  <title>N+1 쿼리 측정</title>
                  <style>
                    body { margin: 0; background: #f4f5f7; color: #1c1c1c;
                           font-family: "Segoe UI", sans-serif; }
                    main { max-width: 880px; margin: 48px auto; background: #fff;
                           padding: 40px 48px; border: 1px solid #e2e4e8; }
                    h1 { margin: 0 0 8px; font-size: 28px; }
                    .when { color: #5c6570; margin-bottom: 28px; }
                    .lead { font-size: 18px; }
                    .nums { display: flex; gap: 32px; margin: 28px 0; }
                    .label { color: #5c6570; font-size: 16px; }
                    .num { font-size: 64px; font-weight: 700; line-height: 1.1; }
                    .before { color: #9b1c1c; }
                    .after { color: #0f6b3c; }
                    .meta { color: #5c6570; margin-top: 8px; }
                    .saved { font-size: 20px; }
                  </style>
                </head>
                <body>
                  <main>
                    <h1>N+1 쿼리 측정</h1>
                    <div class="when">측정 시각 %s</div>
                    %s
                  </main>
                </body>
                </html>
                """.formatted(measuredAt, body);
    }

    private static String format(int value) {
        return String.format(Locale.KOREA, "%,d", value);
    }
}
