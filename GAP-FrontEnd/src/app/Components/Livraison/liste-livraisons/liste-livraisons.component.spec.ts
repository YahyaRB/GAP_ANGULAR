import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListeLivraisonsComponent } from './liste-livraisons.component';

describe('ListeLivraisonsComponent', () => {
  let component: ListeLivraisonsComponent;
  let fixture: ComponentFixture<ListeLivraisonsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ListeLivraisonsComponent]
    });
    fixture = TestBed.createComponent(ListeLivraisonsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
