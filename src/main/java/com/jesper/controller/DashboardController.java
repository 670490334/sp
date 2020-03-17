package com.jesper.controller;


import com.jesper.mapper.OrderMapper;
import com.jesper.model.Order;
import com.jesper.model.Stats;
import com.jesper.redis.DashboardKey;
import com.jesper.redis.RedisService;
import com.jesper.service.SalesOrderService;
import com.jesper.util.RunnableThreadWebCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * 仪表板页面
 *
 * @param model
 * @return
 */

/**
 */
@SuppressWarnings("all")
@Controller
public class DashboardController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SalesOrderService salesOrderService;
    @GetMapping("/user/dashboard")
    public String dashboard(Model model, Stats stats) {

        Long mIncome=100L, lastIncome=20L;
        Integer curOrderNum=1, preOrderNum=10, curRefundOrder, lastRefundOrder, orderNum, orderSum;

        //全部加缓存
        mIncome = salesOrderService.selectCURPayment();
        if (mIncome==null) mIncome = 0L;
//        if (mIncome == null) {
//            mIncome = orderMapper.selectCurPayment();
//            mIncome = mIncome == null ? 0L : mIncome;
//            redisService.set(DashboardKey.board, "mIncome", mIncome);
//        }

        lastIncome = redisService.get(DashboardKey.board, "lastIncome", Long.class);
        if (lastIncome == null) {
            lastIncome = salesOrderService.selectLastPayment();
            lastIncome = lastIncome == null ? 0L : lastIncome;
            redisService.set(DashboardKey.board, "lastIncome", lastIncome);
        }

        curOrderNum = salesOrderService.selectCurOrderNum();
        if (curOrderNum == null) {
            curOrderNum = curOrderNum == null ? 0 : curOrderNum;
//            redisService.set(DashboardKey.board, "curOrderNum", curOrderNum);
        }

        preOrderNum = redisService.get(DashboardKey.board, "preOrderNum", Integer.class);
        if (preOrderNum == null) {
            preOrderNum = salesOrderService.selectLastOrderNum();
            preOrderNum = preOrderNum == null ? 0 : preOrderNum;
            redisService.set(DashboardKey.board, "preOrderNum", preOrderNum);
        }

        curRefundOrder = salesOrderService.selectCurRefundOrder();
        if (curRefundOrder == null) {
            curRefundOrder = curRefundOrder == null ? 0 : curRefundOrder;
        }

        lastRefundOrder = redisService.get(DashboardKey.board, "lastRefundOrder", Integer.class);
        if (lastRefundOrder == null) {
            lastRefundOrder = salesOrderService.selectLastRefundOrder();
            lastRefundOrder = lastRefundOrder == null ? 0 : lastRefundOrder;
            redisService.set(DashboardKey.board, "lastRefundOrder", lastRefundOrder);
        }

        int count = RunnableThreadWebCount.addCount("111");
        stats.setPv(count);//访问量
        stats.setOrderNumPer(getPer(curOrderNum, preOrderNum));//月订单数环比
//        stats.setOrderNumPer(getPer(10, 100));
        stats.setmOrderNum(salesOrderService.selectCurOrderNum());//月订单数
        stats.setmIncome(mIncome);//月收入
//        stats.setmIncome(200);
        stats.setIncomePer(getPer(mIncome, lastIncome));//月收入环比
        stats.setmOrderRefund(curRefundOrder);
        stats.setmOrderRefundPer(getPer(curRefundOrder, lastRefundOrder));

        model.addAttribute("stats", stats);

        List<Integer> data2 = new ArrayList<>();
        List<Integer> data3 = new ArrayList<>();

        Date now = new Date();
        //获取三十天前日期
        Calendar theCa = Calendar.getInstance();
        theCa.setTime(now);
        theCa.add(theCa.DATE, -31);//最后一个数字30可改，30天的意思

        Date temp = new Date();
        Order order = new Order();
        for (int i = 0; i < 31; i++) {
            theCa.add(theCa.DATE, 1);
            temp = theCa.getTime();
            order.setCreateTime(temp);
            //每天的订单数
            orderNum = orderMapper.selectDayOrderNum();
            if (orderNum == null) {
                orderNum = orderMapper.selectDayOrderNum(order);
                orderNum = orderNum == null ? 0 : orderNum;
                redisService.set(DashboardKey.board, "orderNum", orderNum);
            }

            //每天的收入
            orderSum = redisService.get(DashboardKey.board, "orderSum", Integer.class);
            if (orderSum == null) {
                orderSum = orderMapper.selectDayOrderSum(order);
                orderSum = orderSum == null ? 0 : orderSum;
                redisService.set(DashboardKey.board, "orderSum", orderSum);
            }
            data2.add(orderNum);
            data3.add(orderSum);
        }

        model.addAttribute("data2", data2);
        model.addAttribute("data3", data3);
        return "dashboard";
    }

    public String getPer(long a, long b) {
        StringBuilder orderNumPer = new StringBuilder();
        double differ = a - b;
        double d = differ / a;
        String s = String.format("%.2f", d);
        orderNumPer.append(s).append("%");
        return orderNumPer.toString();
    }

    @RequestMapping(value = "/border/website/count/")
    @ResponseBody
    public int count(@RequestParam("key") String key) {
        return RunnableThreadWebCount.addCount(key);
    }
}