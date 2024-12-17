package com.example.dogakunen.controller;

import com.example.dogakunen.repository.entity.AdministratorCSV;
import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.service.DateAttendanceService;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class FileOutputController {
    @Autowired
    DateAttendanceService dateAttendanceService = new DateAttendanceService();


    /*
     * 【整地前】CSVファイル出力（システム管理者用）
     */
    @GetMapping("/csv")
    public ModelAndView csv(@RequestParam(name = "target") String target) {
        ModelAndView mav = new ModelAndView();

        //画面から受け取った年月情報を年と月に分解
        String[] parts = target.split("-");
        Integer year = Integer.parseInt(parts[0]);
        Integer month = Integer.parseInt(parts[1]);
        //取得した年と月から全アカウントの労働時間を取得
        List<AdministratorCSV> results = dateAttendanceService.selectWorkTime(year, month);
        //結果をもとにCSVファイル出力
        try (Writer writer = Files.newBufferedWriter(Paths.get("C:\\Users\\trainee0957\\Desktop\\" + year + "年" + month + "月.csv"))) {
            dateAttendanceService.write(writer, results);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvException e) {
            e.printStackTrace();
        }

        // ホーム画面にリダイレクト
        return new ModelAndView("redirect:/");
    }

}
