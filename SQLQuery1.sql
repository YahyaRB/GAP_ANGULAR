
  update deplacement set flag=1


update ordre_fabrication set qte_rest=quantite where avancement=0
update ordre_fabrication set qte_rest=0 where avancement>=100
UPDATE o 
SET o.qte_rest = (o.quantite - ar.quantite_livre)
FROM ordre_fabrication o
INNER JOIN article ar ON o.article_id = ar.id
WHERE o.qte_rest IS NULL;
UPDATE dbo.ordre_fabrication
SET numof = 'OF ' 
    + CAST(compteur AS VARCHAR(10)) 
    + ' - ' 
    + RIGHT('0' + CAST(MONTH([date_fin]) AS VARCHAR(2)), 2) 
    + ' ' 
    + LOWER(SUBSTRING(creer_par, 1, 1))
      update ordre_fabrication set qte_livre=quantite,qte_rest=0 where avancement>=100
      update ordre_fabrication set qte_livre=0,qte_rest=quantite where avancement=0
      update ordre_fabrication set qte_livre=quantite-qte_rest where avancement>0  and qte_rest>0

