import java.lang.reflect.Field;

import com.twinsoft.convertigo.engine.Context;

public class MakeTypes {

	public static void main(String[] args) {
		StringBuilder sb = new StringBuilder();
		sb.append("/**\n");
		sb.append(" * @typedef {Object} Context\n");
		for (Field field: Context.class.getFields()) {
			Class<?> type = field.getType();
			String t = "any";
			if (type.equals(String.class)) {
				t = "string";
			} else if (type.isPrimitive()) {
				String n = type.getName();
				if (n.equals("boolean")) {
					t = n;
				} else if (n.equals("char")) {
					t = "string";
				} else {
					t = "number";
				}
			}
			sb.append(" * @prop {" + t + "} " + field.getName() + "\n");
		}
		sb.append(" */\n");
		sb.append("/** @type {Context} */\n"); 
		sb.append("var context;");
		System.out.println(sb.toString());
	}

}
