/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */




/** 
   @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

package Observer;

import java.util.logging.*;


// Obtained from http://www.stat.vt.edu/~sundar/java/code/StatFunctions.html
// August 2002

/** * @(#)StatFunctions.java * * DAMAGE (c) 2000 by Sundar Dorai-Raj
  * * @author Sundar Dorai-Raj
  * * Email: sdoraira@vt.edu
  * * This program is free software; you can redistribute it and/or
  * * modify it under the terms of the GNU General Public License 
  * * as published by the Free Software Foundation; either version 2 
  * * of the License, or (at your option) any later version, 
  * * provided that any use properly credits the author. 
  * * This program is distributed in the hope that it will be useful,
  * * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * * GNU General Public License for more details at http://www.gnu.org * * */

import java.io.*;

public final class StatFunctions {


  public static double qnorm(double p,boolean upper) {
    /* Reference:
       J. D. Beasley and S. G. Springer 
       Algorithm AS 111: "The Percentage Points of the Normal Distribution"
       Applied Statistics
    */
    if(p<0 || p>1)
      throw new IllegalArgumentException("Illegal argument "+p+" for qnorm(p).");
    double split=0.42,
           a0=  2.50662823884,
           a1=-18.61500062529,
           a2= 41.39119773534,
           a3=-25.44106049637,
           b1= -8.47351093090,
           b2= 23.08336743743,
           b3=-21.06224101826,
           b4=  3.13082909833,
           c0= -2.78718931138,
           c1= -2.29796479134,
           c2=  4.85014127135,
           c3=  2.32121276858,
           d1=  3.54388924762,
           d2=  1.63706781897,
           q=p-0.5;
    double r,ppnd;
    if(Math.abs(q)<=split) {
      r=q*q;
      ppnd=q*(((a3*r+a2)*r+a1)*r+a0)/((((b4*r+b3)*r+b2)*r+b1)*r+1);
    }
    else {
      r=p;
      if(q>0) r=1-p;
      if(r>0) {
        r=Math.sqrt(-Math.log(r));
        ppnd=(((c3*r+c2)*r+c1)*r+c0)/((d2*r+d1)*r+1);
        if(q<0) ppnd=-ppnd;
      }
      else {
        ppnd=0;
      }
    }
    if(upper) ppnd=1-ppnd;
    return(ppnd);
  }

  public static double qnorm(double p,boolean upper,double mu,double sigma2) {
    return(qnorm(p,upper)*Math.sqrt(sigma2)+mu);
  }

  public static double pnorm(double z,boolean upper) {
    /* Reference:
       I. D. Hill 
       Algorithm AS 66: "The Normal Integral"
       Applied Statistics
    */
    double ltone=7.0,
           utzero=18.66,
           con=1.28,
           a1 = 0.398942280444,
           a2 = 0.399903438504,
           a3 = 5.75885480458,
           a4 =29.8213557808,
           a5 = 2.62433121679,
           a6 =48.6959930692,
           a7 = 5.92885724438,
           b1 = 0.398942280385,
           b2 = 3.8052e-8,
           b3 = 1.00000615302,
           b4 = 3.98064794e-4,
           b5 = 1.986153813664,
           b6 = 0.151679116635,
           b7 = 5.29330324926,
           b8 = 4.8385912808,
           b9 =15.1508972451,
           b10= 0.742380924027,
           b11=30.789933034,
           b12= 3.99019417011;
    double y,alnorm;

    if(z<0) {
      upper=!upper;
      z=-z;
    }
    if(z<=ltone || upper && z<=utzero) {
      y=0.5*z*z;
      if(z>con) {
        alnorm=b1*Math.exp(-y)/(z-b2+b3/(z+b4+b5/(z-b6+b7/(z+b8-b9/(z+b10+b11/(z+b12))))));
      }
      else {
        alnorm=0.5-z*(a1-a2*y/(y+a3-a4/(y+a5+a6/(y+a7))));
      }
    }
    else {
      alnorm=0;
    }
    if(!upper) alnorm=1-alnorm;
    return(alnorm);
  }

  public static double pnorm(double x,boolean upper,double mu,double sigma2) {
    return(pnorm((x-mu)/Math.sqrt(sigma2),upper));
  }

  public static double qt(double p,double ndf,boolean lower_tail) {
    // Algorithm 396: Student's t-quantiles by
    // G.W. Hill CACM 13(10), 619-620, October 1970
    if(p<=0 || p>=1 || ndf<1) 
      throw new IllegalArgumentException("Invalid p or df in call to qt(double,double,boolean).");
    double eps=1e-12;
    double M_PI_2=1.570796326794896619231321691640; // pi/2
    boolean neg;
    double P,q,prob,a,b,c,d,y,x;
    if((lower_tail && p > 0.5) || (!lower_tail && p < 0.5)) {
       neg = false;
       P = 2 * (lower_tail ? (1 - p) : p);
     }
     else {
       neg = true;
       P = 2 * (lower_tail ? p : (1 - p));
     }

     if(Math.abs(ndf - 2) < eps) {   /* df ~= 2 */
       q = Math.sqrt(2 / (P * (2 - P)) - 2);
     }
     else if (ndf < 1 + eps) {   /* df ~= 1 */
       prob = P * M_PI_2;
       q = Math.cos(prob)/Math.sin(prob);
     }
     else {      /*-- usual case;  including, e.g.,  df = 1.1 */
       a = 1 / (ndf - 0.5);
       b = 48 / (a * a);
       c = ((20700 * a / b - 98) * a - 16) * a + 96.36;
       d = ((94.5 / (b + c) - 3) / b + 1) * Math.sqrt(a * M_PI_2) * ndf;
       y = Math.pow(d * P, 2 / ndf);
       if (y > 0.05 + a) {
         /* Asymptotic inverse expansion about normal */
         x = qnorm(0.5 * P,false);
         y = x * x;
         if (ndf < 5)
           c += 0.3 * (ndf - 4.5) * (x + 0.6);
         c = (((0.05 * d * x - 5) * x - 7) * x - 2) * x + b + c;
         y = (((((0.4 * y + 6.3) * y + 36) * y + 94.5) / c - y - 3) / b + 1) * x;
         y = a * y * y;
         if (y > 0.002)/* FIXME: This cutoff is machine-precision dependent*/
           y = Math.exp(y) - 1;
         else { /* Taylor of    e^y -1 : */
           y = (0.5 * y + 1) * y;
         }
       }
       else {
         y = ((1 / (((ndf + 6) / (ndf * y) - 0.089 * d - 0.822)
             * (ndf + 2) * 3) + 0.5 / (ndf + 4))
             * y - 1) * (ndf + 1) / (ndf + 2) + 1 / y;
       }
       q = Math.sqrt(ndf * y);
     }
     if(neg) q = -q;
     return q;
  }

  public static double pt(double t,double df) {
    // ALGORITHM AS 3  APPL. STATIST. (1968) VOL.17, P.189
    // Computes P(T<t)
    double a,b,idf,im2,ioe,s,c,ks,fk,k;
    double g1=0.3183098862;// =1/pi;
    if(df<1)
      throw new IllegalArgumentException("Illegal argument df for pt(t,df).");
    idf=df;
    a=t/Math.sqrt(idf);
    b=idf/(idf+t*t);
    im2=df-2;
    ioe=idf%2;
    s=1;
    c=1;
    idf=1;
    ks=2+ioe;
    fk=ks;
    if(im2>=2) {
      for(k=ks;k<=im2;k+=2) {
        c=c*b*(fk-1)/fk;
        s+=c;
        if(s!=idf) {
          idf=s;
          fk+=2;
        }
      }
    }
    if(ioe!=1)
      return 0.5+0.5*a*Math.sqrt(b)*s;
    if(df==1) s=0;
    return 0.5+(a*b*s+Math.atan(a))*g1;
  }  
}

