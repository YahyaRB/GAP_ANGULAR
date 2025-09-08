update ordre_fabrication set qte_rest=quantite where avancement=0
update ordre_fabrication set qte_rest=0 where avancement>=100
UPDATE o 
SET o.qte_rest = (o.quantite - ar.quantite_livre)
FROM ordre_fabrication o
INNER JOIN article ar ON o.article_id = ar.id
WHERE o.qte_rest IS NULL;