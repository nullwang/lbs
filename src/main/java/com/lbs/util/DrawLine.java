package com.lbs.util;

import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author nullwang@hotmail.com
 *         created at 2014/5/30
 */
public class DrawLine extends JApplet {

    public static void main(String[] args) {
        new DrawLine();
    }

    public DrawLine() {
        this.setSize(400, 400);
        String line = "114.700012,16.212037;114.483032,16.088042;114.191895,16.056371;114.10675,16.016776;113.930969,15.842463;113.854065,15.681221;113.69751,15.615101;113.719482,15.578065;113.873291,15.448386;114.005127,15.400728;114.263306,15.411319;114.631348,15.593939;114.944458,16.003576;114.823608,16.227861;114.700012,16.217312;114.403381,16.072207;114.318237,16.077486;114.700012,16.212037";
        Poly poly = new Poly(line);
        this.getContentPane().add(poly);
        this.setVisible(true);
    }

    public void init() {

    }


    public class Poly extends JPanel {

        Path2D path = new Path2D.Double();

        private float scale = 1;

        public Poly(String line) {

            String[] points = StringUtils.split(line, ";");

            Double[] xes = new Double[points.length];
            Double[] yes = new Double[points.length];
            int i = 0;

            boolean isFirst = true;
            for (String point : points) {
                String[] p = StringUtils.split(point, ",");
                double x = (Double.parseDouble(p[0])) ;
                double y = (Double.parseDouble(p[1]));
                xes[i] = x;
                yes[i++] = y;
                if (isFirst) {
                    path.moveTo(x, y);
                    isFirst = false;
                } else {
                    path.lineTo(x, y);
                }
            }

            Collections.sort(Arrays.asList(xes));
            Collections.sort(Arrays.asList(yes));

            System.out.println(xes[0]+","+xes[xes.length-1]);
            System.out.println(yes[0]+","+yes[yes.length-1]);


            path.closePath();

            this.addMouseWheelListener(new MouseAdapter() {

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    double delta = 0.05f * e.getPreciseWheelRotation();
                    scale += delta;
                    revalidate();
                    repaint();
                }

            });
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g.create();
            g.setColor(Color.gray);
            g2d.scale(scale, scale);
            // g.drawRect(10, 10, 100, 100);
            g2d.draw(path);
            //g2d.fill(path);
            g2d.dispose();

        }

    }
}
