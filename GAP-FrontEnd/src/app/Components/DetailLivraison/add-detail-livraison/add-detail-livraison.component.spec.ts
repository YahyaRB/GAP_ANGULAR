import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddDetailLivraisonComponent } from './add-detail-livraison.component';

describe('AddDetailLivraisonComponent', () => {
  let component: AddDetailLivraisonComponent;
  let fixture: ComponentFixture<AddDetailLivraisonComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AddDetailLivraisonComponent]
    });
    fixture = TestBed.createComponent(AddDetailLivraisonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
