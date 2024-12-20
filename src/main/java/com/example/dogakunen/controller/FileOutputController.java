package com.example.dogakunen.controller;

import com.example.dogakunen.repository.entity.AdministratorCSV;
import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.service.DateAttendanceService;
import com.opencsv.exceptions.CsvException;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class FileOutputController {
    @Autowired
    HttpSession session;

    @Autowired
    DateAttendanceService dateAttendanceService = new DateAttendanceService();

    @Autowired
    AttendanceController attendanceController = new AttendanceController();


    /*
     * CSVファイル出力（システム管理者用）
     */
    @GetMapping("/csv")
    public ModelAndView csv(@RequestParam(name = "target") String target) {
        ModelAndView mav = new ModelAndView();

        //バリデーション
        //エラーメッセージの準備
        List<String> errorMessages = new ArrayList();
        //年月が指定されなかったとき
        if(StringUtils.isBlank(target)){
            errorMessages.add("・対象月を選択してください");
            session.setAttribute("errorMessages", errorMessages);
            return new ModelAndView("redirect:/systemManage");
        }
        //画面から受け取った年月情報を年と月に分解
        String[] parts = target.split("-");
        Integer year = Integer.parseInt(parts[0]);
        Integer month = Integer.parseInt(parts[1]);
        //年月が未来だったとき
        //現在日付を取得
        LocalDate today = LocalDate.now();
        // 現在の月と年を取得
        int Year = today.getYear();
        int Month = today.getMonthValue();
        //現在の年月と指定された年月を比較
        if(year > Year || (year == Year && month > Month)){
            errorMessages.add("・未来のファイルは出力できません");
            session.setAttribute("errorMessages", errorMessages);
            return new ModelAndView("redirect:/systemManage");
        }

        //所定時間の算出
        //１日の所定時間を８時間に設定
        int dailyWorkHours = 8;
        //画面から受け取った年と月と１日の所定時間をもとに月の所定時間を算出
        int time = attendanceController.calculateWorkingHours(year, month, dailyWorkHours);
        //月の所定時間をDuration型に変換
        Duration Time = Duration.ofHours(time);

        //取得した年と月から全アカウントの労働時間を取得
        List<AdministratorCSV> results = dateAttendanceService.selectWorkTime(year, month, Time);
        //結果をもとにCSVファイル出力
        try (Writer writer = Files.newBufferedWriter(Paths.get("C:\\Users\\trainee0919\\Desktop\\" + year + "年" + month + "月.csv"))) {
            dateAttendanceService.write(writer, results);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvException e) {
            e.printStackTrace();
        }

        // システム管理画面にリダイレクト
        return new ModelAndView("redirect:/systemManage");
    }

}
