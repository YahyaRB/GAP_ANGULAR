import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListeFonctionsComponent } from './liste-fonctions.component';

describe('ListeFonctionsComponent', () => {
  let component: ListeFonctionsComponent;
  let fixture: ComponentFixture<ListeFonctionsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ListeFonctionsComponent]
    });
    fixture = TestBed.createComponent(ListeFonctionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
