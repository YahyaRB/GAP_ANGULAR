import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteLivraisonComponent } from './delete-livraison.component';

describe('DeleteLivraisonComponent', () => {
  let component: DeleteLivraisonComponent;
  let fixture: ComponentFixture<DeleteLivraisonComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DeleteLivraisonComponent]
    });
    fixture = TestBed.createComponent(DeleteLivraisonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
