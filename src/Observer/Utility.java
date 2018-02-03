package Observer;

public class Utility {
	/*  CRITZ  --  Compute critical normal z value to
    produce given p.  We just do a bisection
    search for a value within CHI_EPSILON,
    relying on the monotonicity of pochisq().  */
	public double Z_MAX = 6;                    // Maximum z value
	public double ROUND_FLOAT = 6;              // Decimal places to round numbers

	public double critz(double p) {
		double Z_EPSILON = 0.000001;     /* Accuracy of z approximation */
		double minz = -Z_MAX;
		double maxz = Z_MAX;
		double zval = 0.0;
		double pval;

		if (p < 0.0 || p > 1.0) {
			return -1;
		}

		while ((maxz - minz) > Z_EPSILON) {
			pval = poz(zval);
			if (pval > p) {
				maxz = zval;
			} else {
				minz = zval;
			}
			zval = (maxz + minz) * 0.5;
		}
		return(zval);
	}

	public double poz(double z) {
		double y, x, w;

		if (z == 0.0) {
			x = 0.0;
		} else {
			y = 0.5 * Math.abs(z);
			if (y > (Z_MAX * 0.5)) {
				x = 1.0;
			} else if (y < 1.0) {
				w = y * y;
				x = ((((((((0.000124818987 * w
						- 0.001075204047) * w + 0.005198775019) * w
						- 0.019198292004) * w + 0.059054035642) * w
						- 0.151968751364) * w + 0.319152932694) * w
						- 0.531923007300) * w + 0.797884560593) * y * 2.0;
			} else {
				y -= 2.0;
				x = (((((((((((((-0.000045255659 * y
						+ 0.000152529290) * y - 0.000019538132) * y
						- 0.000676904986) * y + 0.001390604284) * y
						- 0.000794620820) * y - 0.002034254874) * y
						+ 0.006549791214) * y - 0.010557625006) * y
						+ 0.011630447319) * y - 0.009279453341) * y
						+ 0.005353579108) * y - 0.002141268741) * y
						+ 0.000535310849) * y + 0.999936657524;
			}
		}
		return z > 0.0 ? ((x + 1.0) * 0.5) : ((1.0 - x) * 0.5);
	}

	
	public double calc_q(double q) {
		double z = 0.0;
		if (q < 0 ||
				q > 1) {
			System.out.println("Probability (Q) must be between 0 and 1.");
		} else {
			z = Math.abs(critz(q));
			//String str = z + "";
			//z = trimfloat(str, ROUND_FLOAT);
		}
		return z;
	}
	
	public double trimfloat(String n, double digits)
	{
		int dp;
		String nn;
		int i;

		dp = n.indexOf(".");
		if (dp != -1) {
			nn = n.substring(0, dp + 1);
			dp++;
			for (i = 0; i < digits; i++) {
				if (dp < n.length()) {
					nn += n.charAt(dp);
					dp++;
				} else {
					break;
				}
			}

			/* Now we want to round the number.  If we're not at
               the end of number and the next character is a digit
               >= 5 add 10^-digits to the value so far. */

			if (dp < n.length() && n.charAt(dp) >= '5' &&
					n.charAt(dp) <= '9') {
				double rd = 0.1, rdi;

				for (rdi = 1; rdi < digits; rdi++) {
					rd *= 0.1;
				}
				rd += Double.parseDouble(nn);

				String srd = rd+"";
				nn = srd.substring(0, nn.length());
				nn += "";
			}

			//  Ditch trailing zeroes in decimal part

			while (nn.length() > 0 && nn.charAt(nn.length() - 1) == '0') {
				nn = nn.substring(0, nn.length() - 1);
			}

			//  Skip excess decimal places before exponent

			while (dp < n.length() && n.charAt(dp) >= '0' &&
					n.charAt(dp) <= '9') {
				dp++;
			}

			//  Append exponent, if any

			if (dp < n.length()) {
				nn += n.substring(dp, n.length());
			}
			n = nn;
		}
		return Double.parseDouble(n);
	}
}
