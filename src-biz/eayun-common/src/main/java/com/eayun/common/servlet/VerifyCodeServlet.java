package com.eayun.common.servlet;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

@SuppressWarnings({ "serial", "restriction" })
public class VerifyCodeServlet extends HttpServlet {
    
    private static final Logger log = LoggerFactory.getLogger(VerifyCodeServlet.class);
    // 验证码长度
    private final int                CODE_LENGTH = 4;

    private static char[]            captchars   = new char[] { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9'                       };
    private Random                   rand        = new Random();

    // 高度，设置旋转后，高度设为30，否则设置成25，效果好一些
    private int                      Height      = 32;
    private int                      width       = 70;
    private StringBuffer             code        = new StringBuffer("");

    /**
     * 生成随机颜色
     * 
     * @param ll
     *            产生颜色值下限(lower limit)
     * @param ul
     *            产生颜色值上限(upper limit)
     * @return 生成的随机颜色对象
     */
    private Color getRandColor(int ll, int ul) {
        if (ll > 255)
            ll = 255;
        if (ll < 1)
            ll = 1;
        if (ul > 255)
            ul = 255;
        if (ul < 1)
            ul = 1;
        if (ul == ll)
            ul = ll + 1;
        int r = rand.nextInt(ul - ll) + ll;
        int g = rand.nextInt(ul - ll) + ll;
        int b = rand.nextInt(ul - ll) + ll;
        Color color = new Color(r, g, b);
        return color;
    }

    private BufferedImage getImage() {
        this.code.setLength(0);
        BufferedImage image = new BufferedImage(width, Height, BufferedImage.TYPE_INT_RGB);

        // 获取图形上下文
        Graphics graphics = image.getGraphics();
        Graphics2D g2 = (Graphics2D) graphics;
        // 背景顔色
        g2.setColor(this.getRandColor(254, 255));
        g2.fillRect(0, 0, width, Height);
        // 边框
        g2.setColor(getRandColor(0, 20));
        //g2.drawRect(0, 0, width - 1, Height - 1);
        // 干扰点
        for (int i = 0; i < 30; i++) {
            g2.setColor(new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
            int x0 = rand.nextInt(width);
            int y0 = rand.nextInt(Height);
            g2.drawLine(x0, y0, x0, y0);
            g2.drawLine(x0 + 1, y0 + 1, x0 + 1, y0 + 1);
            g2.drawLine(x0 + 1, y0, x0 + 1, y0);
            g2.drawLine(x0, y0 + 1, x0, y0 + 1);

        }
        double oldrot = 0;
        // 生成随机码
        for (int i = 0; i < CODE_LENGTH; i++) {
            g2.setFont(new Font(null, Font.BOLD + Font.ITALIC, 24));
            double rot = -0.25 + Math.abs(Math.toRadians(rand.nextInt(25)));
            // 旋转
            g2.rotate(-oldrot, 15, 27);
            oldrot = rot;
            int x0 = 10 * i + 5 + rand.nextInt(5);
            g2.rotate(rot, x0, 27);

            float stroke = Math.abs(rand.nextFloat() % 30);
            g2.setStroke(new BasicStroke(stroke));
            String temp = String.valueOf(captchars[rand.nextInt(captchars.length)]);
            code.append(temp);
            g2.setColor(new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
            g2.setColor(getRandColor(1, 200));
            g2.drawString(temp, 14 * i + 6, 20);
        }
        // 5条干扰线
        for (int i = 0; i < 5; i++) {
            g2.setColor(new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
            g2.drawLine(rand.nextInt(width), rand.nextInt(Height), rand.nextInt(width),
                rand.nextInt(Height));
        }

        // 图像生效
        g2.dispose();
        return image;
    }

    public void printImage(HttpServletRequest request, HttpServletResponse response) {
    	String type=request.getParameter("type");
    	if(!StringUtils.isEmpty(type)){
    		// 将ContentType设为"image/jpeg"，让浏览器识别图像格式。
    		response.setContentType("image/jpg");
    		// 设置页面不缓存
    		response.setHeader("Pragma", "No-cache");
    		response.setHeader("Cache-Control", "no-cache");
    		response.setDateHeader("Expires", 2000);
    		
    		// 获得验证码的图像数据
    		BufferedImage bi = this.getImage();
    		// 把验证码存入session
    		HttpSession session = request.getSession();
    		JSONObject json =new JSONObject();
    		json.put("code", code.toString());
    		json.put("startTime", System.currentTimeMillis());
    		session.setAttribute(type, json.toJSONString());
    		ServletOutputStream outStream = null;
    		try {
    			// 获得Servlet输出流
    			outStream = response.getOutputStream();
    			// 创建可用来将图像数据编码为JPEG数据流的编码器
    			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(outStream);
    			// 将图像数据进行编码
    			encoder.encode(bi);
    			// 强行将缓冲区的内容输入到页面
    			outStream.flush();
    			
    		} catch (IOException ex) {
    		    log.error(ex.getMessage(), ex);
    		} finally {
    			if (outStream != null) {
    				try {
    					// 关闭输出流
    					outStream.close();
    					outStream = null;
    				} catch (IOException iex) {
    				    log.error(iex.getMessage(), iex);
    					outStream = null;
    				}
    			}
    		}
    	}
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        printImage(request, response);
    }
}
