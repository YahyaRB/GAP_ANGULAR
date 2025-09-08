package ma.gap.entity;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CombinedView {
	private Nomenclature nomenclature;
	private NomenclatureArticleAch nomenclatureArticleAch;
	private ArticleAch articleAch;
	private Plan plan;
}
