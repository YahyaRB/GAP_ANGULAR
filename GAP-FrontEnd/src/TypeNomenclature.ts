export const TypeNomenclature = {

  Bois: { value: 'Bois', label: 'Bois' },
  Quincaillerie: { value: 'Quincaillerie', label: 'Quincaillerie' },
  Tole: { value: 'Tôle', label: 'Tôle' },
  Accessoire: { value: 'Accessoire', label: 'Accessoire' },
  Profile: { value: 'Profilé', label: 'Profilé' },
  Vitrage: { value: 'Vitrage', label: 'Vitrage' },
  Panneau: { value: 'Panneau', label: 'Panneau' },
  Cablage : { value: 'Câblage', label: 'Câblage' },
  Appareillage: { value: 'Appareillage', label: 'Appareillage' },
  Eclairage : { value: 'Éclairage', label: 'Éclairage' },
  Tableau: { value: 'Tableau', label: 'Tableau' },



};
export const TYPE_BOIS = [TypeNomenclature.Bois, TypeNomenclature.Quincaillerie];
export const TYPE_ALUMINIUM = [TypeNomenclature.Accessoire,TypeNomenclature.Profile,TypeNomenclature.Vitrage,TypeNomenclature.Panneau];
export const TYPE_ELECTRICITE = [TypeNomenclature.Cablage,TypeNomenclature.Appareillage,TypeNomenclature.Eclairage,TypeNomenclature.Tableau];
export const TYPE_METALLIQUE = [ TypeNomenclature.Tole,TypeNomenclature.Accessoire];
