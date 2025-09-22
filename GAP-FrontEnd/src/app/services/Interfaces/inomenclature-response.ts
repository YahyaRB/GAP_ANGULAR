import {INomenclature} from "./inomenclature";

export interface INomenclatureResponse {
  success: boolean;
  data: INomenclature[];
  message?: string;
  total?: number;
  page?: number;
  size?: number;
}
