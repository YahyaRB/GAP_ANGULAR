import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListeLivraisonLogComponent } from './liste-livraison-log.component';

describe('ListeLivraisonLogComponent', () => {
  let component: ListeLivraisonLogComponent;
  let fixture: ComponentFixture<ListeLivraisonLogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ListeLivraisonLogComponent]
    });
    fixture = TestBed.createComponent(ListeLivraisonLogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
