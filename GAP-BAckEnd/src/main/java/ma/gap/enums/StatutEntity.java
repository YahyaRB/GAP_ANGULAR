package ma.gap.enums;

public enum StatutEntity {
	SAISI(1),
	CONFIRME(2),
	LANCE(3),
	CLOTURE(4),
	SUSPENDU(5),
	ANNULE(6);
	public final int valeur;
	StatutEntity(int valeur) {
		// TODO Auto-generated constructor stub
		this.valeur=valeur;
	}

}
